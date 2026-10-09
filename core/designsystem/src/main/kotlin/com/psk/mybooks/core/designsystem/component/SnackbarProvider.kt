package com.psk.mybooks.core.designsystem.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 어느 화면에서든 한 줄로 스낵바를 띄우는 손잡이. [SnackbarProvider] 아래에서 [LocalSnackbar] 로 얻는다.
 *
 * ```
 * val snackbar: SnackbarHandle = LocalSnackbar.current
 * snackbar.show(stringResource(R.string.saved), SnackbarType.Success)
 * ```
 *
 * - 새 메시지가 오면 떠 있던 것을 닫고 바로 보여 준다. (연속된 오류가 쌓여 밀리지 않는다)
 * - 손잡이는 호스트(화면 전체)와 같은 수명이라 호출한 화면이 사라져도 스낵바가 끊기지 않는다.
 * - 호스트가 없으면(프리뷰, 테스트) 아무 일도 하지 않는다.
 */
@Stable
class SnackbarHandle internal constructor(
    private val hostState: SnackbarHostState?,
    private val scope: CoroutineScope
) {

    /**
     * 스낵바를 띄운다.
     *
     * @param actionLabel 오른쪽 동작 버튼 글자(예: 다시 시도). null 이면 버튼이 없다.
     * @param onAction 동작 버튼을 눌렀을 때 호출된다. 닫히거나 다른 메시지로 바뀌어 사라지면 호출되지 않는다.
     */
    fun show(
        message: String,
        type: SnackbarType = SnackbarType.None,
        actionLabel: String? = null,
        onAction: () -> Unit = {},
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        val host: SnackbarHostState = hostState ?: return
        scope.launch {
            host.currentSnackbarData?.dismiss()
            val result: SnackbarResult = host.showMessage(message, type, actionLabel, duration)
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }
}

/** 호스트가 없을 때의 손잡이. 아무 일도 하지 않으므로 코루틴 스코프는 쓰이지 않는다. */
private val NoOpSnackbarHandle: SnackbarHandle =
    SnackbarHandle(hostState = null, scope = CoroutineScope(Dispatchers.Unconfined))

/** 가장 가까운 [SnackbarProvider] 의 손잡이. 없으면 아무 일도 하지 않는 손잡이다. */
val LocalSnackbar: ProvidableCompositionLocal<SnackbarHandle> = staticCompositionLocalOf { NoOpSnackbarHandle }

/**
 * 아래 화면들이 [LocalSnackbar] 로 스낵바를 띄울 수 있게 한다. 스낵바를 그리는 [MbSnackbarHost] 와 같은 [hostState] 를 쓴다.
 * 앱에서 한 곳(화면 전체를 감싸는 Scaffold)에만 둔다.
 *
 * ```
 * val hostState: SnackbarHostState = remember { SnackbarHostState() }
 * SnackbarProvider(hostState) {
 *     Scaffold(snackbarHost = { MbSnackbarHost(hostState) }) { ... }
 * }
 * ```
 */
@Composable
fun SnackbarProvider(hostState: SnackbarHostState, content: @Composable () -> Unit) {
    val scope: CoroutineScope = rememberCoroutineScope()
    val handle: SnackbarHandle = remember(hostState, scope) { SnackbarHandle(hostState, scope) }

    CompositionLocalProvider(LocalSnackbar provides handle, content = content)
}
