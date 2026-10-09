package com.psk.mybooks.core.designsystem.component

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnackbarHandleTest {

    private fun TestScope.handle(hostState: SnackbarHostState?): SnackbarHandle =
        SnackbarHandle(hostState, backgroundScope)

    private fun SnackbarHostState.shownType(): SnackbarType? =
        (currentSnackbarData?.visuals as? MbSnackbarVisuals)?.type

    @Test
    fun `메시지와 종류를 띄운다`() = runTest {
        val hostState: SnackbarHostState = SnackbarHostState()

        handle(hostState).show("저장했습니다", SnackbarType.Success)
        runCurrent()

        val visuals: SnackbarVisuals? = hostState.currentSnackbarData?.visuals
        assertEquals("저장했습니다", visuals?.message)
        assertEquals(SnackbarType.Success, hostState.shownType())
    }

    @Test
    fun `새 메시지가 오면 떠 있던 것을 닫고 바로 새 것을 보여 준다`() = runTest {
        val hostState: SnackbarHostState = SnackbarHostState()
        val handle: SnackbarHandle = handle(hostState)

        handle.show("첫 번째", SnackbarType.None)
        runCurrent()
        handle.show("두 번째", SnackbarType.Failed)
        runCurrent()

        assertEquals("두 번째", hostState.currentSnackbarData?.visuals?.message)
        assertEquals(SnackbarType.Failed, hostState.shownType())
    }

    @Test
    fun `동작 버튼을 누르면 onAction 이 호출된다`() = runTest {
        val hostState: SnackbarHostState = SnackbarHostState()
        var actionCount: Int = 0

        handle(hostState).show("실패했습니다", SnackbarType.Failed, actionLabel = "다시 시도", onAction = { actionCount++ })
        runCurrent()
        assertEquals("다시 시도", hostState.currentSnackbarData?.visuals?.actionLabel)
        hostState.currentSnackbarData?.performAction()
        runCurrent()

        assertEquals(1, actionCount)
    }

    @Test
    fun `닫히거나 다른 메시지로 바뀌면 onAction 은 호출되지 않는다`() = runTest {
        val hostState: SnackbarHostState = SnackbarHostState()
        var actionCount: Int = 0
        val handle: SnackbarHandle = handle(hostState)

        handle.show("첫 번째", actionLabel = "다시 시도", onAction = { actionCount++ })
        runCurrent()
        handle.show("두 번째")
        runCurrent()
        hostState.currentSnackbarData?.dismiss()
        runCurrent()

        assertEquals(0, actionCount)
        assertNull(hostState.currentSnackbarData)
    }

    @Test
    fun `호스트가 없으면 아무 일도 하지 않는다`() = runTest {
        handle(null).show("아무도 보지 않는 메시지")
        runCurrent()

        assertTrue(true)
    }
}
