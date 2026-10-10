package com.psk.mybooks.dev

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.reflect.ComposableMethod
import androidx.compose.runtime.reflect.getDeclaredComposableMethod
import androidx.compose.ui.Modifier
import com.psk.mybooks.R
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/** 열 Preview 의 `클래스.함수` 이름을 담는 Intent extra */
internal const val EXTRA_PREVIEW: String = "composable"

/**
 * @Preview 함수 하나를 실제 기기에서 전체 화면으로 보여 준다. Compose 도구의 `PreviewActivity` 는 상태 표시줄과
 * 내비게이션 바 아래까지 그려서 위·아래가 가려지므로, 시스템 바 여백을 적용한 이 화면에 대신 띄운다.
 * Preview 함수는 `private` 이어도 이름으로 찾아 호출한다. 다이얼로그 Preview 는 뒤로가기로 닫히지 않아 오른쪽 위에 닫기 버튼을 둔다.
 */
class DevPreviewActivity : ComponentActivity() {

    private var closeOverlay: Dialog? = null

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
                    PreviewContent(className, methodName)
                }
            }
        }
        // 다이얼로그 Preview 는 자기 창이 화면과 뒤로가기를 가져가서 이 화면으로는 닫을 수 없다.
        // 다이얼로그 창이 뜬 뒤에 그 위에 작은 닫기 창을 하나 더 띄운다.
        Handler(Looper.getMainLooper()).postDelayed(::showCloseOverlay, CLOSE_OVERLAY_DELAY_MILLIS)
    }

    override fun onDestroy() {
        closeOverlay?.dismiss()
        closeOverlay = null
        super.onDestroy()
    }

    /**
     * 오른쪽 위에 "닫기" 버튼 하나만 있는 창. 포커스를 받지 않아 아래 Preview(다이얼로그 포함)의 뒤로가기와 입력을 막지 않고,
     * 버튼 바깥을 누르면 그대로 아래로 전달된다.
     */
    private fun showCloseOverlay() {
        if (isFinishing || isDestroyed) return
        val density: Float = resources.displayMetrics.density
        val button: TextView = TextView(this).apply {
            text = getString(R.string.dev_close)
            setTextColor(Color.WHITE)
            setBackgroundColor(OVERLAY_BACKGROUND)
            setPadding((16 * density).toInt(), (8 * density).toInt(), (16 * density).toInt(), (8 * density).toInt())
            setOnClickListener { finish() }
        }
        closeOverlay = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(button)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                addFlags(
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                )
                setGravity(Gravity.TOP or Gravity.END)
                attributes = attributes.apply { y = statusBarHeight() }
            }
            show()
        }
    }

    /** 상태 표시줄 높이(px). 닫기 버튼이 그 아래에 놓이게 한다. */
    private fun statusBarHeight(): Int {
        val id: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }
}

/** 다이얼로그 Preview 의 창이 먼저 뜨도록 기다리는 시간 */
private const val CLOSE_OVERLAY_DELAY_MILLIS: Long = 500
private const val OVERLAY_BACKGROUND: Int = 0xCC000000.toInt()

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
