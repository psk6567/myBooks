package com.psk.mybooks.core.designsystem.component

import android.view.View
import android.view.ViewParent
import android.view.Window
import android.view.WindowManager
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 아래에서 올라오는 시트. 둥근 윗모서리 박스 안에 handle 과 [content] 가 들어간다.
 * 화면에서는 보통 이 함수 대신 [rememberBottomSheet] 를 쓴다.
 *
 * 아래로 끌기, 뒷배경 터치, 뒤로가기는 [onDismissRequest] 로 전달된다.
 * 시트가 열리면 키보드를 내린다. 닫혀도 다시 올리지 않는다. (다시 입력하려면 입력창을 누른다)
 *
 * 시트 안에 슬라이더처럼 끌기를 쓰는 컨트롤을 넣으면 손가락이 조금만 위아래로 흔들려도 시트가 따라 움직인다.
 * 그런 컨트롤은 [lockBottomSheetWhilePressed] 로 누르고 있는 동안 시트의 끌기를 잠글 수 있다. ([MbRangeSlider] 는 이미 그렇게 한다)
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
    val keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
    // 시트가 열리면 키보드는 내린다. (시트가 키보드에 가려지지 않게)
    LaunchedEffect(Unit) { keyboardController?.hide() }
    val onDismiss: () -> Unit = onDismissRequest
    val dismiss: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }
    // 시트 안의 컨트롤(슬라이더 등)이 끌기를 쓰는 동안 시트가 같이 움직이지 않도록 시트의 끌기를 잠글 수 있게 한다
    var gesturesEnabled: Boolean by remember { mutableStateOf(true) }
    val gestures: BottomSheetGestures = remember { BottomSheetGestures { enabled -> gesturesEnabled = enabled } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MyBooksTheme.colors.card,
        contentColor = MyBooksTheme.colors.textPrimary,
        tonalElevation = 0.dp,
        sheetGesturesEnabled = gesturesEnabled,
        dragHandle = { Handle() }
    ) {
        KeepImeTargetOnMainWindow()
        val columnScope: ColumnScope = this
        CompositionLocalProvider(LocalBottomSheetGestures provides gestures) {
            columnScope.content(dismiss)
        }
    }
}

/**
 * 시트 창이 키보드(IME)의 대상 창이 되지 않게 한다. 시트 안에서 불러야 한다.
 *
 * 시트는 별도 창이라, 그대로 두면 열릴 때 IME 대상이 시트 창으로 바뀌고 닫힐 때 다시 메인 창으로 돌아온다.
 * 일부 기기(Galaxy S25, One UI 8.5 / Android 16)는 이렇게 대상이 메인 창으로 돌아오는 순간 키보드를 잠깐 올렸다 내린다.
 * (시스템 로그 `ImeTracker` 에 `IME_REQUESTED_CHANGED_LISTENER` 표시 요청 뒤 `HIDE_UNSPECIFIED_WINDOW` 숨김이 찍힌다)
 * `FLAG_ALT_FOCUSABLE_IM` 을 주면 시트 창이 포커스는 가지되(뒤로가기·바깥 터치 동작) IME 대상은 되지 않아
 * 대상이 바뀌지 않고, 깜빡임도 없다. 시트 안에서는 키보드를 쓸 수 없게 되므로 시트에 텍스트 입력을 두지 않는다.
 */
@Composable
private fun KeepImeTargetOnMainWindow() {
    val view: View = LocalView.current
    DisposableEffect(view) {
        var parent: ViewParent? = view.parent
        while (parent != null && parent !is DialogWindowProvider) parent = parent.parent
        val window: Window? = (parent as? DialogWindowProvider)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        onDispose { }
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

/**
 * 시트 안의 컨트롤이 시트 자체의 끌기(위아래로 움직이기, 내려서 닫기)를 잠시 막고 푸는 손잡이.
 * 직접 쓰기보다 [lockBottomSheetWhilePressed] 를 쓴다.
 */
@Stable
class BottomSheetGestures internal constructor(
    private val setEnabled: (Boolean) -> Unit
) {
    fun lock() = setEnabled(false)

    fun unlock() = setEnabled(true)
}

/** 가장 가까운 [BottomSheet] 의 끌기 잠금 손잡이. 시트 안이 아니면 null 이다. */
val LocalBottomSheetGestures: ProvidableCompositionLocal<BottomSheetGestures?> = staticCompositionLocalOf { null }

/**
 * 이 요소를 누르고 있는 동안 바깥 [BottomSheet] 의 끌기를 잠그고, 손을 떼면 푼다. 시트 안이 아니면 아무 일도 하지 않는다.
 * 터치를 소비하지 않고 지켜보기만 해서 요소 자신의 터치 처리에는 영향이 없다.
 *
 * ```
 * Slider(modifier = Modifier.lockBottomSheetWhilePressed())
 * ```
 */
@Composable
fun Modifier.lockBottomSheetWhilePressed(): Modifier {
    val gestures: BottomSheetGestures = LocalBottomSheetGestures.current ?: return this
    return pointerInput(gestures) {
        try {
            awaitPointerEventScope {
                while (true) {
                    // Initial 단계에서 지켜보기만 한다(소비하지 않음). 누른 손가락이 하나라도 있으면 잠근다.
                    val event: PointerEvent = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.changes
                            .any { it.pressed }
                    ) {
                        gestures.lock()
                    } else {
                        gestures.unlock()
                    }
                }
            }
        } finally {
            // 누른 채로 화면에서 사라져도(예: 시트가 닫힘) 잠금이 남지 않게 한다
            gestures.unlock()
        }
    }
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
