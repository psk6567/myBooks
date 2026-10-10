package com.psk.mybooks.dev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.reflect.ComposableMethod
import androidx.compose.runtime.reflect.getDeclaredComposableMethod
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/** 열 Preview 의 `클래스.함수` 이름을 담는 Intent extra */
internal const val EXTRA_PREVIEW: String = "composable"

/** 위쪽 뒤로가기 줄의 높이. 아래 Preview 는 이만큼 내려서 그린다. */
private val TOP_BAR_HEIGHT: Dp = 64.dp

/**
 * @Preview 함수 하나를 실제 기기에서 전체 화면으로 보여 준다. Compose 도구의 `PreviewActivity` 는 상태 표시줄과
 * 내비게이션 바 아래까지 그려서 위·아래가 가려지므로, 시스템 바 여백을 적용한 이 화면에 대신 띄운다.
 * Preview 함수는 `private` 이어도 이름으로 찾아 호출한다.
 *
 * 위쪽에 Preview 이름과 뒤로가기가 있는 줄이 있다. (다이얼로그 Preview 는 창을 만들지 않고 내용만 그리도록 만들어 두어 이 줄이 가려지지 않는다)
 */
class DevPreviewActivity : ComponentActivity() {

    @OptIn(ExperimentalComposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val name: String = intent.getStringExtra(EXTRA_PREVIEW) ?: return finish()
        val className: String = name.substringBeforeLast('.')
        val methodName: String = name.substringAfterLast('.')
        setContent {
            MyBooksTheme {
                // 시스템 바를 피해 그린다. 바깥 배경은 앱 배경색이라 Preview 가 자기 배경을 그리지 않아도 읽을 수 있다.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MyBooksTheme.colors.background)
                        .systemBarsPadding()
                ) {
                    Box(modifier = Modifier.padding(top = TOP_BAR_HEIGHT)) {
                        PreviewContent(className, methodName)
                    }
                    PreviewTopBar(title = methodName, onBack = ::finish, modifier = Modifier.align(Alignment.TopCenter))
                }
            }
        }
    }
}

/** Preview 이름과 뒤로가기 버튼이 있는 줄 */
@Composable
private fun PreviewTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    TitleBar(
        label = title,
        onBackClick = onBack,
        windowInsets = WindowInsets(0),
        modifier = modifier
            .fillMaxWidth()
            .height(TOP_BAR_HEIGHT)
    )
}

@OptIn(ExperimentalComposeApi::class)
@Composable
private fun PreviewContent(className: String, methodName: String) {
    val method: ComposableMethod = Class
        .forName(className)
        .getDeclaredComposableMethod(methodName)
    // Preview 함수는 private 이라 호출하려면 접근을 열어 두어야 한다
    method
        .asMethod()
        .isAccessible = true
    method.invoke(currentComposer, null)
}
