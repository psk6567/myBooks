package com.psk.mybooks.dev

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/** 열 Preview 의 `클래스.함수` 이름을 담는 Intent extra */
internal const val EXTRA_PREVIEW: String = "composable"

/** 위쪽 뒤로가기 줄의 높이. 아래 Preview 는 이만큼 내려서 그린다. */
private val TOP_BAR_HEIGHT: Dp = 64.dp

/** 다이얼로그 Preview 의 창이 먼저 뜨도록 기다리는 시간 */
private const val TOP_BAR_OVERLAY_DELAY_MILLIS: Long = 500

/**
 * @Preview 함수 하나를 실제 기기에서 전체 화면으로 보여 준다. Compose 도구의 `PreviewActivity` 는 상태 표시줄과
 * 내비게이션 바 아래까지 그려서 위·아래가 가려지므로, 시스템 바 여백을 적용한 이 화면에 대신 띄운다.
 * Preview 함수는 `private` 이어도 이름으로 찾아 호출한다.
 *
 * 위쪽에 뒤로가기 줄이 있다. 다이얼로그 Preview 는 자기 창이 화면 전체와 터치·뒤로가기를 가져가서 이 화면의 줄을 누를 수 없으므로,
 * 다이얼로그가 뜬 뒤 같은 줄을 그 위의 별도 창으로 한 번 더 띄운다. (눈에는 하나의 줄로 보인다)
 */
class DevPreviewActivity : ComponentActivity() {

    private var topBarOverlay: Dialog? = null

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
        Handler(Looper.getMainLooper()).postDelayed({ showTopBarOverlay(methodName) }, TOP_BAR_OVERLAY_DELAY_MILLIS)
    }

    override fun onDestroy() {
        topBarOverlay?.dismiss()
        topBarOverlay = null
        super.onDestroy()
    }

    /**
     * 뒤로가기 줄만 있는 별도 창을 다이얼로그 위에 띄운다. 포커스를 받지 않아 아래 Preview 의 뒤로가기를 막지 않고,
     * 줄 바깥의 터치는 그대로 아래로 전달된다. 창의 기준 위치가 이미 상태 표시줄 아래라 따로 내릴 필요가 없다.
     */
    private fun showTopBarOverlay(title: String) {
        if (isFinishing || isDestroyed) return
        val view: ComposeView = ComposeView(this).apply {
            setContent { MyBooksTheme { PreviewTopBar(title = title, onBack = ::finish) } }
        }
        topBarOverlay = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(view)
            window?.apply {
                // 별도 창에서 Compose 를 그리려면 이 화면의 생명주기와 상태 저장소를 연결해 주어야 한다
                decorView.setViewTreeLifecycleOwner(this@DevPreviewActivity)
                decorView.setViewTreeSavedStateRegistryOwner(this@DevPreviewActivity)
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                )
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setGravity(Gravity.TOP)
            }
            show()
        }
    }
}

/** Preview 이름과 뒤로가기 버튼이 있는 줄. 이 화면과 다이얼로그 위 창이 같은 모양을 쓴다. */
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
