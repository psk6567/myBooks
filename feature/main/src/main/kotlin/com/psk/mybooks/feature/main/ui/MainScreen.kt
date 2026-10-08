package com.psk.mybooks.feature.main.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psk.mybooks.core.designsystem.component.Icon
import com.psk.mybooks.core.designsystem.component.TabBar
import com.psk.mybooks.core.designsystem.component.TabItem
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.feature.main.R

/** 하단 탭 순서. 선택 상태는 이 순서의 index 로 저장한다. */
internal enum class MainTab(@StringRes val labelRes: Int) {
    Search(R.string.feature_main_tab_search),
    Favorite(R.string.feature_main_tab_favorite)
}

/** ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다. */
@Composable
fun MainRoute(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()
    var selectedIndex by rememberSaveable { mutableIntStateOf(MainTab.Search.ordinal) }

    MainScreen(
        selectedTab = MainTab.entries[selectedIndex],
        onTabSelected = { selectedIndex = it.ordinal },
        modifier = modifier
    )
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun MainScreen(selectedTab: MainTab, onTabSelected: (MainTab) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = MyBooksTheme.colors.background,
        topBar = { TitleBar(label = stringResource(selectedTab.labelRes)) },
        bottomBar = {
            TabBar(
                tabs = rememberTabItems(),
                selectedIndex = selectedTab.ordinal,
                onTabSelected = { onTabSelected(MainTab.entries[it]) }
            )
        }
    ) { innerPadding ->
        // 탭별 화면(검색, 즐겨찾기)은 화면이 만들어지면 여기에 연결한다.
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
    }
}

@Composable
private fun rememberTabItems(): List<TabItem> {
    val searchLabel = stringResource(MainTab.Search.labelRes)
    val favoriteLabel = stringResource(MainTab.Favorite.labelRes)

    return remember(searchLabel, favoriteLabel) {
        listOf(
            TabItem(label = searchLabel) { selected ->
                Icon(
                    icon = MbIcon.Search,
                    // 라벨이 함께 읽히므로 아이콘 설명은 생략한다
                    contentDescription = null,
                    tint = if (selected) MyBooksTheme.colors.icon else MyBooksTheme.colors.iconInactive
                )
            },
            TabItem(label = favoriteLabel) { selected ->
                Icon(
                    icon = MbIcon.Favorite,
                    contentDescription = null,
                    tint = if (selected) MyBooksTheme.colors.favorite else MyBooksTheme.colors.iconInactive
                )
            }
        )
    }
}

@Preview(name = "Search tab", showSystemUi = true)
@Composable
private fun MainScreenSearchPreview() {
    MyBooksTheme {
        MainScreen(selectedTab = MainTab.Search, onTabSelected = {})
    }
}

@Preview(name = "Favorite tab (Dark)", showSystemUi = true)
@Composable
private fun MainScreenFavoriteDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        MainScreen(selectedTab = MainTab.Favorite, onTabSelected = {})
    }
}
