package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer

/** 다이얼로그 버튼 구성. 한 개짜리와 두 개짜리를 타입으로 나눠, 맞지 않는 조합을 만들 수 없게 한다. */
sealed interface DialogButtons {

    /**
     * 확인 버튼 하나. 알림처럼 확인만 받으면 되는 경우.
     *
     * @param onConfirm 확인을 눌렀을 때. 다이얼로그를 닫는 것은 호출한 쪽이 한다.
     */
    data class One(
        val confirmText: String,
        val onConfirm: () -> Unit
    ) : DialogButtons

    /**
     * 확인과 취소 두 개. 선택을 받아야 하는 경우. 취소가 왼쪽, 확인이 오른쪽이다.
     *
     * @param onConfirm 확인을 눌렀을 때. 다이얼로그를 닫는 것은 호출한 쪽이 한다.
     * @param onDismiss 취소를 눌렀을 때. 다이얼로그를 닫는 것은 호출한 쪽이 한다.
     */
    data class Two(
        val confirmText: String,
        val dismissText: String,
        val onConfirm: () -> Unit,
        val onDismiss: () -> Unit
    ) : DialogButtons
}

/**
 * 화면을 막고 확인이나 선택을 받는 다이얼로그. 열려 있는지는 호출한 쪽이 정한다. (이 함수를 그릴 때만 보인다)
 *
 * 사용법 (닫는 것은 호출한 쪽이 한다):
 * ```
 * var showDialog: Boolean by remember { mutableStateOf(false) }
 * if (showDialog) {
 *     MbDialog(
 *         title = "즐겨찾기 삭제",
 *         message = "이 도서를 즐겨찾기에서 삭제할까요?",
 *         buttons = DialogButtons.Two(
 *             confirmText = "삭제",
 *             dismissText = "취소",
 *             onConfirm = { showDialog = false; /* 삭제 */ },
 *             onDismiss = { showDialog = false }
 *         ),
 *         onDismissRequest = { showDialog = false }
 *     )
 * }
 * ```
 * 확인 버튼 하나만 필요하면 `DialogButtons.One(confirmText, onConfirm)` 을 넘긴다.
 *
 * @param title 제목. null 이면 메시지만 보인다.
 * @param buttons 버튼 구성. [DialogButtons.One] 또는 [DialogButtons.Two].
 * @param onDismissRequest 바깥을 누르거나 뒤로가기를 눌러 닫으려 할 때. [cancelable] 이 false 면 호출되지 않는다.
 * @param cancelable false 면 바깥을 누르거나 뒤로가기를 눌러도 닫히지 않고 버튼으로만 닫힌다.
 * 사용자가 반드시 확인해야 하는 오류에 쓴다.
 */
@Composable
fun MbDialog(
    message: String,
    buttons: DialogButtons,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    cancelable: Boolean = true
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = cancelable,
            dismissOnClickOutside = cancelable,
            // 기본 폭(좁은 고정 폭) 대신 화면 폭에 맞춰 좌우 여백을 준다
            usePlatformDefaultWidth = false
        )
    ) {
        DialogContent(title = title, message = message, buttons = buttons, modifier = modifier)
    }
}

/**
 * 다이얼로그 상자의 내용(제목, 메시지, 버튼). 창([Dialog])과 분리해 두어서 Preview 가 창을 만들지 않고 이것만 그릴 수 있다.
 * (창은 화면 전체와 뒤로가기를 가져가서 Preview 도구나 개발자 모드에서 다루기 어렵다)
 */
@Composable
private fun DialogContent(
    message: String,
    buttons: DialogButtons,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Column(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .widthIn(max = 400.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.card)
            .padding(24.dp)
    ) {
        if (title != null) {
            Text(text = title, style = typography.bold18, color = colors.textPrimary)
            VSpacer(12.dp)
        }
        Text(text = message, style = typography.normal14, color = colors.textSecondary)
        VSpacer(24.dp)
        DialogButtonRow(buttons)
    }
}

@Composable
private fun DialogButtonRow(buttons: DialogButtons) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        when (buttons) {
            is DialogButtons.One -> ActionButton(
                text = buttons.confirmText,
                onClick = buttons.onConfirm,
                modifier = Modifier.weight(1f)
            )

            is DialogButtons.Two -> {
                ActionButton(
                    text = buttons.dismissText,
                    onClick = buttons.onDismiss,
                    primary = false,
                    modifier = Modifier.weight(1f)
                )
                ActionButton(
                    text = buttons.confirmText,
                    onClick = buttons.onConfirm,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/** Preview 는 창 없이 상자만 그린다. 바탕은 다이얼로그가 떠 있을 때처럼 화면 뒤에 깔리는 색을 흉내 낸다. */
@Composable
private fun DialogPreviewFrame(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MyBooksTheme.colors.listBackground)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Preview(name = "Dialog one button", widthDp = 360)
@Composable
private fun MbDialogOnePreview() {
    MyBooksTheme {
        DialogPreviewFrame {
            DialogContent(
                title = "알림",
                message = "인증에 실패했습니다. 앱 설정을 확인해 주세요.",
                buttons = DialogButtons.One(confirmText = "확인", onConfirm = {})
            )
        }
    }
}

@Preview(name = "Dialog two buttons", widthDp = 360)
@Composable
private fun MbDialogTwoPreview() {
    MyBooksTheme {
        DialogPreviewFrame {
            DialogContent(
                title = "즐겨찾기 삭제",
                message = "이 도서를 즐겨찾기에서 삭제할까요?",
                buttons = DialogButtons.Two(confirmText = "삭제", dismissText = "취소", onConfirm = {}, onDismiss = {})
            )
        }
    }
}

@Preview(name = "Dialog message only (Dark)", widthDp = 360)
@Composable
private fun MbDialogDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        DialogPreviewFrame {
            DialogContent(
                message = "네트워크 연결이 끊겨 도서를 불러오지 못했습니다. 연결을 확인한 뒤 다시 시도해 주세요.",
                buttons = DialogButtons.Two(confirmText = "다시 시도", dismissText = "닫기", onConfirm = {}, onDismiss = {})
            )
        }
    }
}
