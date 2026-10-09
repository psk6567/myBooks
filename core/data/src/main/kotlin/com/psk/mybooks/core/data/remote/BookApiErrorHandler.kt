package com.psk.mybooks.core.data.remote

import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response

/** 오류 본문은 짧으므로 이만큼만 읽는다. */
private const val MAX_ERROR_BODY_BYTES: Long = 8 * 1024

private const val HTTP_UNAUTHORIZED: Int = 401
private const val HTTP_FORBIDDEN: Int = 403
private const val HTTP_NOT_FOUND: Int = 404
private const val HTTP_TOO_MANY_REQUESTS: Int = 429
private const val HTTP_BAD_REQUEST: Int = 400
private const val HTTP_SERVER_ERROR: Int = 500

/** 카카오 공통 에러 코드 중 앱이 구분해서 다루는 것. https://developers.kakao.com/docs/ko/rest-api/error-code */
private const val CODE_UNAUTHORIZED: Int = -401
private val CODES_QUOTA: Set<Int> = setOf(-10, -11)
private val CODES_FORBIDDEN: Set<Int> = setOf(-3, -4, -5, -6, -12, -13)
private val CODES_SERVER: Set<Int> = setOf(-1, -7)

/**
 * 오류 본문. 문서의 공통 에러는 `code`/`msg` 이고, 실제 검색 API 는 `errorType`/`message` 로 준다.
 * 둘 다 받고, 서버 메시지(`msg`, `message`)는 읽지 않는다. (키가 포함될 수 있다. [BookApiException] 참고)
 */
@Serializable
private data class ErrorBody(
    val code: Int? = null,
    val errorType: String? = null
)

/**
 * 도서 검색 API 의 오류 응답을 [BookApiException] 으로 바꾼다.
 * 응답이 2xx 가 아니면 본문의 에러 코드와 HTTP 상태로 종류를 정해 던지고, 성공 응답은 그대로 통과시킨다.
 *
 * 카카오 API 용 OkHttpClient 에만 붙인다. 앱 공용 클라이언트에 붙이면 이미지 로더 등 다른 요청까지 영향을 받는다.
 * 연결 실패나 시간 초과처럼 응답 자체가 없는 오류는 건드리지 않고 그대로 던져진다.
 */
internal class BookApiErrorHandler : Interceptor {

    /** 오류 본문에는 읽지 않는 필드가 있으므로 모르는 키는 무시한다. 앱 공용 Json 설정에 기대지 않는다. */
    private val json: Json = Json { ignoreUnknownKeys = true }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        if (response.isSuccessful) return response

        val status: Int = response.code
        val body: String = try {
            response.peekBody(MAX_ERROR_BODY_BYTES).string()
        } catch (e: IOException) {
            ""
        }
        response.close()

        val error: ErrorBody? = try {
            json.decodeFromString<ErrorBody>(body)
        } catch (e: IllegalArgumentException) {
            // JSON 이 아닌 본문(게이트웨이의 HTML 오류 페이지 등)은 HTTP 상태로만 판단한다
            null
        }
        throw toException(status, error?.code, error?.errorType)
    }
}

/** 에러 코드가 있으면 코드를 먼저, 없으면 HTTP 상태로 종류를 정한다. */
private fun toException(status: Int, code: Int?, type: String?): BookApiException = when {
    status == HTTP_TOO_MANY_REQUESTS || code in CODES_QUOTA -> BookApiException.QuotaExceeded(status, code, type)
    status == HTTP_UNAUTHORIZED || code == CODE_UNAUTHORIZED -> BookApiException.Unauthorized(status, code, type)
    status == HTTP_FORBIDDEN || code in CODES_FORBIDDEN -> BookApiException.Forbidden(status, code, type)
    status >= HTTP_SERVER_ERROR || code in CODES_SERVER -> BookApiException.ServerError(status, code, type)
    status == HTTP_BAD_REQUEST || status == HTTP_NOT_FOUND -> BookApiException.InvalidRequest(status, code, type)
    else -> BookApiException.Unknown(status, code, type)
}
