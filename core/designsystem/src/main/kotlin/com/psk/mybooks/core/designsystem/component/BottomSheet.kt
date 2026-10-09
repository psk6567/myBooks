package com.psk.mybooks.core.designsystem.component

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 아래에서 올라오는 시트. 둥근 윗모서리 박스 안에 handle 과 [content] 가 들어간다.
 * 화면에서는 보통 이 함수 대신 [rememberBottomSheet] 를 쓴다.
 *
 * 아래로 끌기, 뒷배경 터치, 뒤로가기는 [onDismissRequest] 로 전달된다.
 * 아래 화면의 텍스트 필드에 포커스가 있으면 시트가 닫힐 때 키보드를 올린다. (포커스가 없으면 아무 일도 없다)
 *
 * @param content 시트 내용. 파라미터로 받은 `dismiss` 를 부르면 닫힘 애니메이션 후 [onDismissRequest] 가 호출된다.
 * 항목을 고르고 닫을 때 쓴다. (그냥 [onDismissRequest] 쪽 상태만 바꾸면 애니메이션 없이 사라진다)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope: CoroutineScope = rememberCoroutineScope()
    // 시트 바깥(메인 창)의 View 와 키보드 컨트롤러
    val view: View = LocalView.current
    val keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
    // 시트는 별도 창이라, 닫히면서 창 포커스가 돌아올 때 포커스가 남은 입력창에 키보드가 잠깐 떴다가
    // 메인 창의 "키보드 숨김" 상태가 다시 적용되어 내려간다. 닫기 전에 표시를 요청해 두어 깜빡이지 않게 한다.
    // 입력 중인 텍스트 필드가 없을 때 요청하면 오히려 키보드가 깜빡이므로, 있을 때만 요청한다.
    // TODO: 특정 기기(Galaxy S25, One UI 8.5 / Android 16)에서 텍스트 필드 포커스 없이 시트를 닫으면
    //  키보드가 약 0.04초 나타났다 사라진다. 별도 창이 닫힐 때 시스템이 보내는 키보드 요청으로, 다른 기기에서는 재현되지 않는다.
    //  같은 창에 직접 그리는 시트로 해소되는 것은 확인했으나, 표준 컴포넌트 유지를 위해 보류한다.
    val onDismiss: () -> Unit = {
        if (view.onCheckIsTextEditor()) keyboardController?.show()
        onDismissRequest()
    }
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MyBooksTheme.colors.card,
        contentColor = MyBooksTheme.colors.textPrimary,
        tonalElevation = 0.dp,
        dragHandle = { Handle() }
    ) {
        content(dismiss)
    }
}

@Composable
private fun Handle() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MyBooksTheme.colors.border)
    )
}

/**
 * 화면에서 시트를 선언하고, 반환된 손잡이로 연다. 화면마다 `if` 나 열림 상태를 두지 않아도 된다.
 *
 * ```
 * val sortSheet: BottomSheetHandle = rememberBottomSheet { dismiss -> ... }
 * SortChip(onClick = { sortSheet.show() })
 * ```
 *
 * - 열림 여부는 rememberSaveable 로 저장되어 화면 회전, 프로세스 종료 후에도 복원된다.
 * - [content] 는 매번 다시 그려지므로 열려 있는 동안 바뀐 상태도 바로 반영된다.
 * - ViewModel 이 열 시점을 정해야 하면 Effect 를 받아 `show()` 를 부른다.
 *
 * @param onDismiss 시트가 닫힌 뒤 호출된다. (dismiss 호출, 뒷배경 터치, 끌어내리기, 뒤로가기 모두)
 * @param content 시트 내용. 받은 `dismiss` 를 부르면 닫힘 애니메이션 후 닫힌다.
 */
@Composable
fun rememberBottomSheet(
    onDismiss: () -> Unit = {},
    content: @Composable ColumnScope.(dismiss: () -> Unit) -> Unit
): BottomSheetHandle {
    val isShown: MutableState<Boolean> = rememberSaveable { mutableStateOf(false) }
    val handle: BottomSheetHandle = remember(isShown) { BottomSheetHandle(isShown) }

    // ModalBottomSheet 은 별도 창에 그려지므로 이 함수를 부른 위치의 레이아웃에는 영향이 없다
    if (isShown.value) {
        BottomSheet(
            onDismissRequest = {
                isShown.value = false
                onDismiss()
            },
            content = content
        )
    }
    return handle
}

/** [rememberBottomSheet] 가 돌려주는 손잡이 */
@Stable
class BottomSheetHandle internal constructor(
    private val isShown: MutableState<Boolean>
) {
    fun show() {
        isShown.value = true
    }
}
