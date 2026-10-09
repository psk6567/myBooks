package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer

/**
 * 왼쪽에 세로로 놓는 탭 모음. 태블릿이나 펼친 폴더블처럼 넓은 화면에서 아래쪽 [TabBar] 대신 쓴다.
 * [TabBar] 와 같은 [TabItem] 을 받아서 같은 탭 목록을 두 곳에서 그대로 쓸 수 있다.
 *
 * @param windowInsets 시스템 바 여백. 기본은 왼쪽(가로 모드의 내비게이션 바)이고, 위쪽은 호출하는 쪽의 상단 바가 처리한다.
 */
@Composable
fun TabRail(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Start)
) {
    val colors: MbColors = MyBooksTheme.colors

    Row(modifier = modifier.fillMaxHeight().background(colors.background)) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                .width(80.dp)
                .padding(top = 8.dp)
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected: Boolean = index == selectedIndex
                val labelColor: Color = if (selected) colors.icon else colors.iconInactive
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .selectable(
                            selected = selected,
                            // 탭은 터치 효과(ripple) 없이 선택 상태로만 표현한다
                            interactionSource = null,
                            indication = null,
                            role = Role.Tab,
                            onClick = { onTabSelected(index) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    tab.icon(selected)
                    VSpacer(2.dp)
                    Text(text = tab.label, style = MyBooksTheme.typography.normal10, color = labelColor)
                }
            }
        }
        VerticalDivider(thickness = 1.dp, color = colors.divider)
    }
}

@Preview(name = "TabRail", widthDp = 120, heightDp = 360)
@Composable
private fun TabRailPreview() {
    MyBooksTheme {
        var selected: Int by remember { mutableIntStateOf(0) }
        TabRail(
            tabs = rememberSampleTabs(),
            selectedIndex = selected,
            onTabSelected = { selected = it },
            windowInsets = WindowInsets(0)
        )
    }
}
