package com.psk.mybooks.core.data.remote

import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BookApiErrorHandlerTest {

    /** 핸들러 뒤에서 정해 둔 상태와 본문으로 응답하는 클라이언트. 네트워크를 쓰지 않는다. */
    private fun call(status: Int, body: String): Response {
        val client: OkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(BookApiErrorHandler())
            .addInterceptor { chain ->
                Response
                    .Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(status)
                    .message("test")
                    .body(body.toResponseBody("application/json".toMediaType()))
                    .build()
            }.build()
        return client
            .newCall(
                Request
                    .Builder()
                    .url("https://dapi.kakao.com/v3/search/book?query=a")
                    .build()
            ).execute()
    }

    /** 오류 응답이면 던져진 예외를 돌려준다. */
    private fun failure(status: Int, body: String): BookApiException {
        try {
            call(status, body).close()
        } catch (e: BookApiException) {
            return e
        }
        fail("HTTP $status 인데 예외가 던져지지 않았다")
        throw AssertionError()
    }

    @Test
    fun `성공 응답은 그대로 통과하고 본문도 읽을 수 있다`() {
        val response: Response = call(200, """{"documents":[],"meta":{}}""")

        assertEquals(200, response.code)
        assertEquals(
            """{"documents":[],"meta":{}}""",
            response.body
                .string()
        )
    }

    @Test
    fun `실제 검색 API 형식(errorType, message)의 401 은 인증 오류다`() {
        val e: BookApiException =
            failure(401, """{"errorType":"AccessDeniedError","message":"wrong appKey(abc) format"}""")

        assertTrue(e is BookApiException.Unauthorized)
        assertEquals(401, e.httpStatus)
        assertEquals("AccessDeniedError", e.errorType)
        assertNull(e.errorCode)
    }

    @Test
    fun `서버 메시지에 키가 들어 있어도 예외 메시지에는 남지 않는다`() {
        val e: BookApiException =
            failure(401, """{"errorType":"AccessDeniedError","message":"wrong appKey(SECRET-KEY) format"}""")

        assertFalse(
            e.message
                .orEmpty()
                .contains("SECRET-KEY")
        )
    }

    @Test
    fun `문서 형식(code, msg)의 에러 코드로 종류를 나눈다`() {
        assertTrue(failure(401, """{"code":-401,"msg":"invalid key"}""") is BookApiException.Unauthorized)
        assertTrue(failure(403, """{"code":-3,"msg":"x"}""") is BookApiException.Forbidden)
        assertTrue(failure(403, """{"code":-5,"msg":"x"}""") is BookApiException.Forbidden)
        assertTrue(failure(400, """{"code":-13,"msg":"x"}""") is BookApiException.Forbidden)
        assertTrue(failure(400, """{"code":-2,"msg":"x"}""") is BookApiException.InvalidRequest)
        assertTrue(failure(400, """{"code":-8,"msg":"x"}""") is BookApiException.InvalidRequest)
        assertTrue(failure(400, """{"code":-10,"msg":"x"}""") is BookApiException.QuotaExceeded)
        assertTrue(failure(400, """{"code":-11,"msg":"x"}""") is BookApiException.QuotaExceeded)
        assertTrue(failure(400, """{"code":-1,"msg":"x"}""") is BookApiException.ServerError)
        assertTrue(failure(400, """{"code":-7,"msg":"x"}""") is BookApiException.ServerError)
    }

    @Test
    fun `에러 코드가 없으면 HTTP 상태로 종류를 정한다`() {
        assertTrue(failure(400, """{"errorType":"InvalidArgument"}""") is BookApiException.InvalidRequest)
        assertTrue(
            failure(404, """{"errorType":"ResourceNotFound","message":"x"}""") is BookApiException.InvalidRequest
        )
        assertTrue(failure(429, "") is BookApiException.QuotaExceeded)
        assertTrue(failure(500, "") is BookApiException.ServerError)
        assertTrue(failure(503, "") is BookApiException.ServerError)
        assertTrue(failure(418, "") is BookApiException.Unknown)
    }

    @Test
    fun `JSON 이 아닌 본문이어도 HTTP 상태로 종류를 정한다`() {
        val e: BookApiException = failure(502, "<html><body>Bad Gateway</body></html>")

        assertTrue(e is BookApiException.ServerError)
        assertEquals(502, e.httpStatus)
        assertNull(e.errorType)
    }

    @Test
    fun `예외는 IOException 이라 OkHttp 가 비동기 호출에서 실패로 전달할 수 있다`() {
        assertTrue(failure(401, "") is IOException)
    }
}
