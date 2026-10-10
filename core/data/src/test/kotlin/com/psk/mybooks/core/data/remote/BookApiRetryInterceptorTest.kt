package com.psk.mybooks.core.data.remote

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class BookApiRetryInterceptorTest {

    /** 시도할 때마다 앞에서부터 하나씩 꺼내 던지고, 목록이 끝나면 성공(200)으로 응답한다. 네트워크를 쓰지 않는다. */
    private class Script(
        private val failures: List<IOException>
    ) {
        var attempts: Int = 0
        val sleeps: MutableList<Long> = mutableListOf()

        fun client(onAttempt: (Call) -> Unit = {}): OkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(BookApiRetryInterceptor(sleep = { millis -> sleeps += millis }))
            .addInterceptor { chain ->
                attempts++
                onAttempt(chain.call())
                failures
                    .getOrNull(attempts - 1)
                    ?.let { throw it }
                Response
                    .Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }.build()
    }

    private fun execute(client: OkHttpClient): Response = client
        .newCall(
            Request
                .Builder()
                .url("https://dapi.kakao.com/v3/search/book?query=a")
                .build()
        ).execute()

    @Test
    fun `처음에 성공하면 한 번만 시도하고 기다리지 않는다`() {
        val script: Script = Script(emptyList())

        execute(script.client()).close()

        assertEquals(1, script.attempts)
        assertTrue(
            script.sleeps
                .isEmpty()
        )
    }

    @Test
    fun `시간 초과가 두 번 나도 세 번째에 성공하면 성공한다`() {
        val script: Script = Script(listOf(SocketTimeoutException("1"), SocketTimeoutException("2")))

        val response: Response = execute(script.client())

        assertEquals(200, response.code)
        response.close()
        assertEquals(3, script.attempts)
        assertEquals(listOf(500L, 1_000L), script.sleeps)
    }

    @Test
    fun `세 번 모두 실패하면 마지막 예외를 던지고 더 시도하지 않는다`() {
        val last: IOException = ConnectException("마지막")
        val script: Script = Script(listOf(ConnectException("1"), ConnectException("2"), last, ConnectException("4")))

        try {
            execute(script.client()).close()
            fail("예외가 던져져야 한다")
        } catch (e: IOException) {
            assertSame(last, e)
        }

        assertEquals(3, script.attempts)
    }

    @Test
    fun `서버 오류(5xx)는 다시 시도한다`() {
        val script: Script = Script(listOf(BookApiException.ServerError(503, null, null)))

        execute(script.client()).close()

        assertEquals(2, script.attempts)
    }

    @Test
    fun `인증, 권한, 잘못된 요청, 호출 횟수 초과는 다시 시도하지 않는다`() {
        listOf(
            BookApiException.Unauthorized(401, null, null),
            BookApiException.Forbidden(403, -3, null),
            BookApiException.InvalidRequest(400, -2, null),
            BookApiException.QuotaExceeded(429, null, null),
            BookApiException.Unknown(418, null, null)
        ).forEach { error ->
            val script: Script = Script(listOf(error))

            try {
                execute(script.client()).close()
                fail("예외가 던져져야 한다")
            } catch (e: IOException) {
                assertSame(error, e)
            }

            assertEquals("${error::class.simpleName} 은 한 번만 시도", 1, script.attempts)
        }
    }

    @Test
    fun `호출이 취소되면 더 시도하지 않는다`() {
        val script: Script = Script(listOf(SocketTimeoutException("1"), SocketTimeoutException("2")))

        try {
            // 첫 시도 중에 취소한다(새 검색을 시작한 경우와 같다)
            execute(script.client { call -> call.cancel() }).close()
            fail("예외가 던져져야 한다")
        } catch (e: IOException) {
            // 취소된 호출은 재시도하지 않고 그대로 실패한다
        }

        assertEquals(1, script.attempts)
        assertTrue(
            script.sleeps
                .isEmpty()
        )
    }
}
