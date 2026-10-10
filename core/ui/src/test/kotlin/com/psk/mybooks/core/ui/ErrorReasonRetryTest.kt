package com.psk.mybooks.core.ui

import com.psk.mybooks.core.model.ErrorReason
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorReasonRetryTest {

    @Test
    fun `일시적인 문제만 다시 시도할 수 있다`() {
        val retryable: List<ErrorReason> = ErrorReason.entries
            .filter { it.isRetryable }

        assertEquals(
            listOf(ErrorReason.Network, ErrorReason.Timeout, ErrorReason.QuotaExceeded, ErrorReason.Server),
            retryable
        )
    }

    @Test
    fun `다시 해도 같은 인증과 권한 오류는 다시 시도할 수 없다`() {
        assertEquals(false, ErrorReason.Unauthorized.isRetryable)
        assertEquals(false, ErrorReason.Forbidden.isRetryable)
        assertEquals(false, ErrorReason.InvalidRequest.isRetryable)
    }
}
