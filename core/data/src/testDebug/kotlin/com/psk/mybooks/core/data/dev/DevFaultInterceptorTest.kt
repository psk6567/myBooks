package com.psk.mybooks.core.data.dev

import java.net.ConnectException
import java.net.SocketTimeoutException
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** debug 빌드에만 있는 개발자 모드 인터셉터가 고른 문제를 일으키는지 확인한다. */
class DevFaultInterceptorTest {

    @After
    fun reset() {
        DevNetwork.select(DevFault.None)
    }

    /** 실제 서버 대신 항상 200 을 돌려주는 끝단 인터셉터 뒤에 [DevFaultInterceptor] 를 둔 클라이언트 */
    private val client: OkHttpClient = OkHttpClient
        .Builder()
        .addInterceptor(DevFaultInterceptor())
        .addInterceptor(
            Interceptor { chain ->
                Response
                    .Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("ok")
                    .body("".toResponseBody())
                    .build()
            }
        ).build()

    private fun call(): Int = client
        .newCall(
            Request
                .Builder()
                .url("https://example.com/")
                .build()
        ).execute()
        .use { it.code }

    @Test
    fun `문제가 없으면 그대로 통과한다`() {
        assertEquals(200, call())
    }

    @Test
    fun `서버 오류와 인증 실패는 해당 HTTP 코드를 돌려준다`() {
        DevNetwork.select(DevFault.ServerError)
        assertEquals(500, call())

        DevNetwork.select(DevFault.Unauthorized)
        assertEquals(401, call())
    }

    @Test
    fun `연결 실패는 ConnectException 을 던진다`() {
        DevNetwork.select(DevFault.ConnectionFailure)

        assertThrows(ConnectException::class.java) { call() }
    }

    @Test
    fun `간헐 실패는 처음 2번만 시간 초과이고 그 뒤 성공한다`() {
        DevNetwork.select(DevFault.FlakyThenOk)

        assertThrows(SocketTimeoutException::class.java) { call() }
        assertThrows(SocketTimeoutException::class.java) { call() }
        assertEquals(200, call())
        assertEquals(200, call())
    }
}
