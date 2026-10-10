package com.psk.mybooks.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.psk.mybooks.core.designsystem.component.DialogButtons
import com.psk.mybooks.core.designsystem.component.LocalSnackbar
import com.psk.mybooks.core.designsystem.component.MbDialog
import com.psk.mybooks.core.designsystem.component.SnackbarHandle
import com.psk.mybooks.core.designsystem.component.SnackbarType
import com.psk.mybooks.core.model.ErrorReason
import com.psk.mybooks.core.model.ErrorSeverity
import com.psk.mybooks.core.model.toErrorReason
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel 이 내보내는 오류([com.psk.mybooks.core.common.MviViewModel.error])를 중요도에 따라 사용자에게 알린다.
 * 화면(Route)에서 한 줄로 쓴다.
 *
 * ```
 * ErrorEffect(viewModel.error)
 * ```
 *
 * - [ErrorSeverity.Low]: 스낵바(실패)로 잠깐 보여 준다. 새 오류가 오면 이전 것을 바로 대체한다.
 * - [ErrorSeverity.High]: 확인 버튼 하나뿐이고 바깥을 눌러도 닫히지 않는 다이얼로그. 확인해야만 넘어간다.
 *
 * 스낵바는 [LocalSnackbar] 의 호스트(앱에서 한 곳)를 쓰므로 그 아래에서 써야 한다.
 */
@Composable
fun ErrorEffect(errors: Flow<Throwable>) {
    val context: android.content.Context = LocalContext.current
    val snackbar: SnackbarHandle = LocalSnackbar.current
    // 회전해도 알림이 사라지지 않도록 저장한다. 닫으면 null 이다.
    var highReason: ErrorReason? by rememberSaveable { mutableStateOf(null) }
    // 수집을 다시 시작하지 않고 최신 값을 쓴다
    val currentSnackbar: SnackbarHandle by rememberUpdatedState(snackbar)

    LaunchedEffect(errors) {
        errors.collect { error ->
            val reason: ErrorReason = error.toErrorReason()
            when (reason.severity) {
                ErrorSeverity.Low ->
                    currentSnackbar.show(context.getString(reason.messageRes()), SnackbarType.Failed)

                ErrorSeverity.High -> highReason = reason
            }
        }
    }

    highReason?.let { reason ->
        MbDialog(
            title = stringResource(R.string.core_ui_error_title),
            message = stringResource(reason.messageRes()),
            buttons = DialogButtons.One(
                confirmText = stringResource(R.string.core_ui_error_confirm),
                onConfirm = { highReason = null }
            ),
            onDismissRequest = {},
            cancelable = false
        )
    }
}

/** 오류 종류별로 사용자에게 보일 문구 */
@StringRes
internal fun ErrorReason.messageRes(): Int = when (this) {
    ErrorReason.Network -> R.string.core_ui_error_network
    ErrorReason.Timeout -> R.string.core_ui_error_timeout
    ErrorReason.Unauthorized -> R.string.core_ui_error_unauthorized
    ErrorReason.Forbidden -> R.string.core_ui_error_forbidden
    ErrorReason.InvalidRequest -> R.string.core_ui_error_invalid_request
    ErrorReason.QuotaExceeded -> R.string.core_ui_error_quota_exceeded
    ErrorReason.Server -> R.string.core_ui_error_server
    ErrorReason.Unknown -> R.string.core_ui_error_unknown
}
