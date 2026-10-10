package com.psk.mybooks.core.data.remote

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/** 처음 시도를 포함한 최대 시도 횟수 (최초 1번 + 재시도 2번) */
private const val MAX_ATTEMPTS: Int = 3

/** 재시도 사이에 기다리는 시간(ms). 첫 재시도 전, 두 번째 재시도 전 순서다. */
private val BACKOFF_MILLIS: List<Long> = listOf(500L, 1_000L)

/**
 * 불안정한 네트워크에서 일시적인 실패를 자동으로 다시 시도한다. 카카오 API 용 OkHttpClient 에만 붙인다.
 *
 * - **다시 시도하는 것**: 연결 실패, 시간 초과, 끊김처럼 응답을 받지 못한 경우와 서버 오류([BookApiException.ServerError], 5xx)
 * - **다시 시도하지 않는 것**: 인증, 권한, 잘못된 요청, 호출 횟수 초과(429)처럼 다시 해도 결과가 같거나 호출 횟수만 더 쓰는 경우.
 *   그 외 [BookApiException] 도 그대로 던진다.
 * - 최대 [MAX_ATTEMPTS] 번 시도하고 사이에 [BACKOFF_MILLIS] 만큼 기다린다. 모두 실패하면 마지막 예외를 던진다.
 * - 호출이 취소되면(새 검색을 시작했을 때 등) 더 시도하지 않는다.
 *
 * [BookApiErrorHandler] 가 던지는 [BookApiException] 도 판단해야 하므로 그보다 바깥(먼저 등록)에 둔다.
 *
 * @param sleep 재시도 전에 기다리는 방법. 테스트에서 실제로 기다리지 않도록 바꿀 수 있다.
 */
internal class BookApiRetryInterceptor(
    private val sleep: (millis: Long) -> Unit = { millis -> Thread.sleep(millis) }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 1
        while (true) {
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                if (attempt >= MAX_ATTEMPTS || !e.isRetryable() || chain
                        .call()
                        .isCanceled()
                ) {
                    throw e
                }
                sleep(BACKOFF_MILLIS[attempt - 1])
                // 기다리는 사이에 취소되었으면 더 시도하지 않는다
                if (chain
                        .call()
                        .isCanceled()
                ) {
                    throw e
                }
                attempt++
            }
        }
    }
}

/** 응답을 받지 못한 실패(연결, 시간 초과, 끊김)는 다시 해 볼 만하고, API 오류는 서버 오류만 그렇다. */
private fun IOException.isRetryable(): Boolean = when (this) {
    is BookApiException.ServerError -> true
    is BookApiException -> false
    else -> true
}
