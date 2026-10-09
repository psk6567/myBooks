package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer

/** 스낵바의 종류. 색과 아이콘이 달라진다. */
enum class SnackbarType {
    /** 일반 안내. 아이콘 없이 중립 색으로 보인다. */
    None,

    /** 성공 (저장했습니다 등). */
    Success,

    /** 실패 (불러오지 못했습니다 등). */
    Failed
}

/** [SnackbarHostState] 가 종류([type])까지 함께 전달하기 위한 값. 화면에서는 [showMessage] 로 만든다. */
class MbSnackbarVisuals(
    override val message: String,
    val type: SnackbarType,
    override val actionLabel: String?,
    override val duration: SnackbarDuration
) : SnackbarVisuals {
    override val withDismissAction: Boolean = false
}

// TODO: 재시도. 에러 종류(ErrorReason)가 서버 오류나 네트워크 없음처럼 다시 해 볼 만할 때만 "다시 시도" 동작 버튼
//  (actionLabel)을 붙이고, 눌리면 ViewModel 에 재시도 Intent 를 보내 Paging 의 retry() 등을 부른다.
//  인증·권한·잘못된 요청은 다시 해도 같으므로 버튼을 붙이지 않는다. 로딩/에러/재시도 UI 단계에서 함께 한다.

/**
 * 스낵바를 띄운다. 닫힐 때까지(또는 [actionLabel] 을 누를 때까지) 기다린 뒤 결과를 돌려준다.
 * 이미 떠 있는 스낵바가 있으면 그것이 끝난 뒤에 보인다. 바로 바꾸려면 호출 전에 `currentSnackbarData?.dismiss()` 를 부른다.
 *
 * @param actionLabel 오른쪽 동작 버튼 글자(예: 다시 시도). null 이면 버튼이 없다.
 */
suspend fun SnackbarHostState.showMessage(
    message: String,
    type: SnackbarType = SnackbarType.None,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult = showSnackbar(MbSnackbarVisuals(message, type, actionLabel, duration))

/**
 * 화면 아래에 스낵바를 그리는 자리. Scaffold 의 `snackbarHost` 에 넣고 [SnackbarHostState] 로 띄운다.
 * [showMessage] 로 띄운 종류에 따라 [MbSnackbar] 모양이 정해진다.
 */
@Composable
fun MbSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data: SnackbarData ->
        val type: SnackbarType = (data.visuals as? MbSnackbarVisuals)?.type ?: SnackbarType.None
        MbSnackbar(
            message = data.visuals.message,
            type = type,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            actionLabel = data.visuals.actionLabel,
            onAction = { data.performAction() }
        )
    }
}

/**
 * 스낵바 모양. 보통은 [MbSnackbarHost] 가 그리므로 직접 쓸 일은 드물다.
 *
 * @param actionLabel 오른쪽 동작 버튼 글자. null 이면 버튼이 없다.
 */
@Composable
fun MbSnackbar(
    message: String,
    type: SnackbarType,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography
    val container: Color = when (type) {
        SnackbarType.None -> colors.snackbar
        SnackbarType.Success -> colors.success
        SnackbarType.Failed -> colors.error
    }
    val content: Color = when (type) {
        SnackbarType.None -> colors.onSnackbar
        SnackbarType.Success -> colors.onSuccess
        SnackbarType.Failed -> colors.onError
    }
    val icon: MbIcon? = when (type) {
        SnackbarType.None -> null
        SnackbarType.Success -> MbIcon.CheckCircle
        SnackbarType.Failed -> MbIcon.Error
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            // 글자가 의미를 전하므로 아이콘 설명은 생략한다
            Icon(icon = icon, contentDescription = null, tint = content, size = 20.dp)
            HSpacer(12.dp)
        }
        Text(text = message, style = typography.normal14, color = content, modifier = Modifier.weight(1f))
        if (actionLabel != null) {
            HSpacer(12.dp)
            Text(
                text = actionLabel,
                style = typography.bold14,
                color = content,
                modifier = Modifier.clickable(role = Role.Button, onClick = onAction)
            )
        }
    }
}

@Preview(name = "Snackbar types", widthDp = 360)
@Composable
private fun MbSnackbarPreview() {
    MyBooksTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp)
        ) {
            MbSnackbar(message = "검색 결과를 불러왔습니다", type = SnackbarType.None)
            MbSnackbar(message = "즐겨찾기에 담았습니다", type = SnackbarType.Success, modifier = Modifier.padding(top = 8.dp))
            MbSnackbar(
                message = "네트워크 연결을 확인해 주세요",
                type = SnackbarType.Failed,
                modifier = Modifier.padding(top = 8.dp),
                actionLabel = "다시 시도"
            )
        }
    }
}

@Preview(name = "Snackbar types (Dark)", widthDp = 360)
@Composable
private fun MbSnackbarDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.background(MyBooksTheme.colors.background).padding(16.dp)
        ) {
            MbSnackbar(message = "검색 결과를 불러왔습니다", type = SnackbarType.None)
            MbSnackbar(message = "즐겨찾기에 담았습니다", type = SnackbarType.Success, modifier = Modifier.padding(top = 8.dp))
            MbSnackbar(
                message = "네트워크 연결을 확인해 주세요",
                type = SnackbarType.Failed,
                modifier = Modifier.padding(top = 8.dp),
                actionLabel = "다시 시도"
            )
        }
    }
}
