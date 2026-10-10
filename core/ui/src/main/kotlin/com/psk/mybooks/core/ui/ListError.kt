package com.psk.mybooks.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.component.ActionButton
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer
import com.psk.mybooks.core.model.AppException
import com.psk.mybooks.core.model.ErrorReason
import com.psk.mybooks.core.model.toErrorReason

/**
 * 목록을 처음 불러오다 실패했을 때 목록 자리에 보이는 안내. 오류 종류별 문구를 보여 주고,
 * 다시 해 볼 만한 오류(네트워크, 시간 초과, 서버 등)면 "다시 시도" 버튼이 함께 보인다.
 * 인증·권한처럼 다시 해도 같은 오류는 버튼 없이 안내만 한다.
 *
 * 목록 안에서 알리므로 같은 오류를 스낵바로 또 띄우지 않는다.
 *
 * @param error 실패한 이유. [AppException] 이 아니면 알 수 없는 오류로 안내한다.
 * @param onRetry 버튼을 눌렀을 때. 보통 Paging 의 `retry()` 를 부른다.
 */
@Composable
fun ListErrorView(error: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val reason: ErrorReason = error.toErrorReason()
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(reason.messageRes()),
            style = typography.normal16,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
        )
        if (reason.isRetryable) {
            VSpacer(16.dp)
            ActionButton(
                text = stringResource(R.string.core_ui_retry),
                onClick = onRetry,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}

/**
 * 목록을 더 불러오다(다음 페이지) 실패했을 때 목록 맨 끝에 보이는 안내. 이미 불러온 항목은 그대로 두고 그 아래에 놓는다.
 * 다시 해 볼 만한 오류면 "다시 시도" 버튼이 있고, 아니면 오류 문구만 보인다.
 *
 * @param onRetry 버튼을 눌렀을 때. 보통 Paging 의 `retry()` 를 부른다.
 */
@Composable
fun ListFooterError(error: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val reason: ErrorReason = error.toErrorReason()
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(if (reason.isRetryable) R.string.core_ui_load_more_failed else reason.messageRes()),
            style = typography.normal14,
            color = colors.textTertiary,
            textAlign = TextAlign.Center
        )
        if (reason.isRetryable) {
            VSpacer(12.dp)
            ActionButton(
                text = stringResource(R.string.core_ui_retry),
                onClick = onRetry,
                modifier = Modifier.width(140.dp)
            )
        }
    }
}

@Preview(name = "ListErrorView", widthDp = 360, heightDp = 400)
@Composable
private fun ListErrorViewPreview() {
    MyBooksTheme {
        ListErrorView(error = AppException(ErrorReason.Timeout), onRetry = {})
    }
}

@Preview(name = "ListErrorView not retryable (Dark)", widthDp = 360, heightDp = 400)
@Composable
private fun ListErrorViewNotRetryablePreview() {
    MyBooksTheme(darkTheme = true) {
        ListErrorView(error = AppException(ErrorReason.Unauthorized), onRetry = {})
    }
}

@Preview(name = "ListFooterError", widthDp = 360)
@Composable
private fun ListFooterErrorPreview() {
    MyBooksTheme {
        ListFooterError(error = AppException(ErrorReason.Network), onRetry = {})
    }
}
