package com.psk.mybooks.core.data.dev

import java.net.ConnectException
import java.net.SocketTimeoutException
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

private const val SLOW_MILLIS: Long = 8_000
private const val TIMEOUT_AFTER_MILLIS: Long = 1_000

/**
 * [DevNetwork] 에서 고른 문제를 도서 검색 API 호출에 일으킨다.
 * 오류 변환(`BookApiErrorHandler`) 바로 안쪽에 두어, 가짜 HTTP 오류 응답이 실제 서버 응답처럼 변환·재시도를 거친다.
 */
internal class DevFaultInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = when (DevNetwork.fault) {
        DevFault.None -> chain.proceed(chain.request())

        DevFault.Slow -> {
            Thread.sleep(SLOW_MILLIS)
            chain.proceed(chain.request())
        }

        DevFault.Timeout -> {
            Thread.sleep(TIMEOUT_AFTER_MILLIS)
            throw SocketTimeoutException("dev: timeout")
        }

        DevFault.ServerError -> fakeResponse(chain, code = 500)

        DevFault.Unauthorized -> fakeResponse(chain, code = 401)

        DevFault.ConnectionFailure -> throw ConnectException("dev: connection failure")

        DevFault.FlakyThenOk ->
            if (DevNetwork.flakyRemaining
                    .getAndUpdate { maxOf(it - 1, 0) } > 0
            ) {
                throw SocketTimeoutException("dev: flaky")
            } else {
                chain.proceed(chain.request())
            }
    }

    private fun fakeResponse(chain: Interceptor.Chain, code: Int): Response = Response
        .Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("dev")
        .body("""{"errorType":"dev","message":"dev"}""".toResponseBody())
        .build()
}
