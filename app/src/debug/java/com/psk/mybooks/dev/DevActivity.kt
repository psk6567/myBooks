package com.psk.mybooks.dev

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.psk.mybooks.R
import com.psk.mybooks.core.data.dev.DevFault
import com.psk.mybooks.core.data.dev.DevNetwork
import com.psk.mybooks.core.designsystem.component.ActionButton
import com.psk.mybooks.core.designsystem.component.OptionItem
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 프로세스를 종료하기 전에 앱이 백그라운드로 가는 것을 기다리는 시간 */
private const val LMK_DELAY_MILLIS: Long = 1_500

/**
 * 개발자 모드. debug 빌드에서만 있고, 메인 화면의 제목을 여러 번 누르면 열린다. ([DevEntry])
 * 평가·개발 중에 재현하기 어려운 상황을 만들어 보는 도구 모음이다.
 * - 네트워크 테스트: 도서 검색 API 가 느리거나 실패하는 상황 (스켈레톤, 재시도, 오류 안내 확인)
 * - Warm start / LMK: 화면만 다시 만들 때와 프로세스가 종료됐다 돌아올 때의 상태 복원 확인
 * - 디자인 시스템 Preview: 디자인 시스템의 @Preview 를 실제 기기에서 열기
 */
class DevActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyBooksTheme {
                DevScreen(
                    onBack = ::finish,
                    onWarmStart = {
                        finish()
                        DevEntry.recreateMain()
                    },
                    onLmk = {
                        // 앱을 백그라운드로 보낸 뒤 프로세스를 끝낸다. 시스템은 메모리 부족으로 정리된 것과 같게 다루어
                        // 다시 열면 저장해 둔 상태(SavedStateHandle)로 복원한다.
                        moveTaskToBack(true)
                        Handler(
                            Looper.getMainLooper()
                        ).postDelayed({ Process.killProcess(Process.myPid()) }, LMK_DELAY_MILLIS)
                    }
                )
            }
        }
    }
}

@Composable
private fun DevScreen(onBack: () -> Unit, onWarmStart: () -> Unit, onLmk: () -> Unit) {
    // 앱 배경색을 직접 깐다. 없으면 다크 모드에서 창 기본 배경(밝은 색)이 보여 글자가 읽히지 않는다
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyBooksTheme.colors.background)
    ) {
        TitleBar(label = stringResource(R.string.dev_title), onBackClick = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
                .padding(16.dp)
        ) {
            NetworkSection()
            Divider()
            LifecycleSection(onWarmStart = onWarmStart, onLmk = onLmk)
            Divider()
            PreviewSection()
        }
    }
}

@Composable
private fun Divider() {
    VSpacer(16.dp)
    HorizontalDivider(color = MyBooksTheme.colors.divider)
    VSpacer(16.dp)
}

@Composable
private fun SectionTitle(text: String, hint: String) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography
    Text(text = text, style = typography.bold16, color = colors.textPrimary)
    VSpacer(4.dp)
    Text(text = hint, style = typography.normal12, color = colors.textSecondary)
    VSpacer(8.dp)
}

@Composable
private fun NetworkSection() {
    var selected: DevFault by remember { mutableStateOf(DevNetwork.fault) }
    val labels: List<Pair<DevFault, Int>> = listOf(
        DevFault.None to R.string.dev_fault_none,
        DevFault.Slow to R.string.dev_fault_slow,
        DevFault.Timeout to R.string.dev_fault_timeout,
        DevFault.ServerError to R.string.dev_fault_server_error,
        DevFault.Unauthorized to R.string.dev_fault_unauthorized,
        DevFault.ConnectionFailure to R.string.dev_fault_connection_failure,
        DevFault.FlakyThenOk to R.string.dev_fault_flaky
    )
    SectionTitle(stringResource(R.string.dev_section_network), stringResource(R.string.dev_network_hint))
    labels.forEach { (fault, label) ->
        OptionItem(
            text = stringResource(label),
            selected = selected == fault,
            onClick = {
                DevNetwork.select(fault)
                selected = fault
            }
        )
    }
}

@Composable
private fun LifecycleSection(onWarmStart: () -> Unit, onLmk: () -> Unit) {
    SectionTitle(stringResource(R.string.dev_section_lifecycle), stringResource(R.string.dev_warm_start_hint))
    ActionButton(
        text = stringResource(R.string.dev_warm_start),
        onClick = onWarmStart,
        modifier = Modifier.fillMaxWidth()
    )
    VSpacer(16.dp)
    Text(
        text = stringResource(R.string.dev_lmk_hint),
        style = MyBooksTheme.typography.normal12,
        color = MyBooksTheme.colors.textSecondary
    )
    VSpacer(8.dp)
    ActionButton(text = stringResource(R.string.dev_lmk), onClick = onLmk, modifier = Modifier.fillMaxWidth())
}

@Composable
private fun PreviewSection() {
    val context: Context = LocalContext.current
    var previews: List<PreviewEntry>? by remember { mutableStateOf(null) }
    LaunchedEffect(Unit) { previews = withContext(Dispatchers.Default) { scanPreviews(context) } }

    SectionTitle(stringResource(R.string.dev_section_preview), stringResource(R.string.dev_preview_hint))
    val found: List<PreviewEntry>? = previews
    when {
        found == null -> Text(
            text = stringResource(R.string.dev_preview_loading),
            style = MyBooksTheme.typography.normal14,
            color = MyBooksTheme.colors.textSecondary
        )

        found.isEmpty() -> Text(
            text = stringResource(R.string.dev_preview_empty),
            style = MyBooksTheme.typography.normal14,
            color = MyBooksTheme.colors.textSecondary
        )

        else -> {
            var lastGroup: String? = null
            found.forEach { entry ->
                if (entry.group != lastGroup) {
                    lastGroup = entry.group
                    VSpacer(12.dp)
                    Text(text = entry.group, style = MyBooksTheme.typography.bold12, color = MyBooksTheme.colors.brand)
                }
                Text(
                    text = entry.method,
                    style = MyBooksTheme.typography.normal14,
                    color = MyBooksTheme.colors.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { openPreview(context, entry) }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

private fun openPreview(context: Context, entry: PreviewEntry) {
    context.startActivity(
        Intent(context, DevPreviewActivity::class.java)
            .putExtra(EXTRA_PREVIEW, entry.composableName)
    )
}
