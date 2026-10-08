package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer

/**
 * TabBar 의 탭 하나.
 *
 * @param icon 아이콘 슬롯. 선택 여부를 받아 탭마다 색을 다르게 줄 수 있다.
 * 디자인 시스템 [Icon] 으로 그리고, 선택되지 않았을 때는 `colors.iconInactive` 를 쓴다.
 */
@Immutable
data class TabItem(val label: String, val icon: @Composable (selected: Boolean) -> Unit)

/**
 * 탭 바. 탭 개수와 내용은 호출하는 쪽이 정한다.
 * [tabs] 는 호출하는 쪽에서 remember 해서 넘긴다. (매번 새 List 를 만들면 다시 그려진다)
 *
 * @param windowInsets 아래 시스템 바 여백. 화면 하단이 아닌 곳에 쓸 때는 `WindowInsets(0)` 을 넘긴다.
 */
@Composable
fun TabBar(
    tabs: List<TabItem>,
    selectedIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = WindowInsets.navigationBars
) {
    val colors = MyBooksTheme.colors

    Column(modifier = modifier.background(colors.background)) {
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(windowInsets)
                .height(56.dp)
                .selectableGroup()
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = index == selectedIndex
                val labelColor = if (selected) colors.icon else colors.iconInactive
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
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
    }
}

/** 미리보기·예시용 탭 목록: 검색은 기본 아이콘 색, 즐겨찾기는 선택 시 favorite 색 */
@Composable
private fun rememberSampleTabs(): List<TabItem> = remember {
    listOf(
        TabItem(label = "검색") { selected ->
            Icon(
                icon = MbIcon.Search,
                // 라벨이 함께 읽히므로 아이콘 설명은 생략한다
                contentDescription = null,
                tint = if (selected) MyBooksTheme.colors.icon else MyBooksTheme.colors.iconInactive
            )
        },
        TabItem(label = "즐겨찾기") { selected ->
            Icon(
                icon = MbIcon.Favorite,
                contentDescription = null,
                tint = if (selected) MyBooksTheme.colors.favorite else MyBooksTheme.colors.iconInactive
            )
        }
    )
}

@Preview(name = "Search selected", widthDp = 360)
@Composable
private fun TabBarPreview() {
    MyBooksTheme {
        var selected by remember { mutableIntStateOf(0) }
        TabBar(
            tabs = rememberSampleTabs(),
            selectedIndex = selected,
            onTabSelected = { selected = it },
            windowInsets = WindowInsets(0)
        )
    }
}

@Preview(name = "Favorite selected (Dark)", widthDp = 360)
@Composable
private fun TabBarDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        TabBar(tabs = rememberSampleTabs(), selectedIndex = 1, onTabSelected = {}, windowInsets = WindowInsets(0))
    }
}
