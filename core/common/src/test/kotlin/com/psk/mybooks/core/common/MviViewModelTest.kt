package com.psk.mybooks.core.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    private class TestViewModel : MviViewModel<Unit, Unit, Nothing>(Unit) {
        override fun onIntent(intent: Unit) = Unit

        fun fail(throwable: Throwable) = sendError(throwable)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `보낸 오류는 구독하면 그대로 전달된다`() = runTest {
        val viewModel: TestViewModel = TestViewModel()
        val error: IllegalStateException = IllegalStateException("실패")

        viewModel.fail(error)

        assertSame(
            error,
            viewModel.error
                .first()
        )
    }

    @Test
    fun `직전과 같은 예외 객체는 다시 보내지 않고 새 예외는 보낸다`() = runTest {
        val viewModel: TestViewModel = TestViewModel()
        val first: Exception = Exception("첫 번째")
        val second: Exception = Exception("두 번째")
        val received: MutableList<Throwable> = mutableListOf()
        backgroundScope.launch {
            viewModel.error
                .collect { received += it }
        }

        viewModel.fail(first)
        viewModel.fail(first)
        viewModel.fail(second)
        viewModel.fail(first)
        // 수집 코루틴은 테스트 스케줄러에서 돌므로 한 번 실행시켜 쌓인 오류를 받게 한다
        runCurrent()

        assertEquals(listOf<Throwable>(first, second, first), received)
    }

    @Test
    fun `구독하기 전에 보낸 오류도 쌓였다가 순서대로 전달된다`() = runTest {
        val viewModel: TestViewModel = TestViewModel()
        val first: Exception = Exception("첫 번째")
        val second: Exception = Exception("두 번째")

        viewModel.fail(first)
        viewModel.fail(second)

        assertSame(
            first,
            viewModel.error
                .first()
        )
        assertSame(
            second,
            viewModel.error
                .first()
        )
    }
}
