package com.psk.mybooks.feature.main.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psk.mybooks.core.designsystem.component.Icon
import com.psk.mybooks.core.designsystem.component.TabBar
import com.psk.mybooks.core.designsystem.component.TabItem
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.feature.favorite.ui.FavoriteRoute
import com.psk.mybooks.feature.search.ui.SearchRoute

/** ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다. */
@Composable
fun MainRoute(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    MainScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        pageContent = { tab ->
            when (tab) {
                MainTab.Search -> SearchRoute()
                MainTab.Favorite -> FavoriteRoute()
            }
        }
    )
}

/**
 * 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상.
 *
 * @param pageContent 탭별 화면. 다른 feature 의 ViewModel 을 직접 만들지 않도록 호출하는 쪽에서 넘긴다.
 */
@Composable
internal fun MainScreen(
    state: MainUiState,
    onIntent: (MainIntent) -> Unit,
    pageContent: @Composable (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = state.selectedTab.ordinal) { MainTab.entries.size }
    val currentOnIntent by rememberUpdatedState(onIntent)

    // 상태(탭 선택) → Pager 이동
    LaunchedEffect(state.selectedTab) {
        pagerState.animateScrollToPage(state.selectedTab.ordinal)
    }
    // Pager 를 넘겨서 페이지가 바뀌면 → Intent
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            currentOnIntent(MainIntent.SelectTab(MainTab.entries[page]))
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MyBooksTheme.colors.background,
        topBar = { TitleBar(label = stringResource(state.selectedTab.labelRes)) },
        bottomBar = {
            TabBar(
                tabs = rememberTabItems(),
                selectedIndex = state.selectedTab.ordinal,
                onTabSelected = { onIntent(MainIntent.SelectTab(MainTab.entries[it])) }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            // 탭이 둘뿐이라 둘 다 유지해서 탭을 오가도 스크롤·입력 상태가 남게 한다
            beyondViewportPageCount = MainTab.entries.size - 1
        ) { page ->
            pageContent(MainTab.entries[page])
        }
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
        MainScreen(state = MainUiState(MainTab.Search), onIntent = {}, pageContent = {})
    }
}

@Preview(name = "Favorite tab (Dark)", showSystemUi = true)
@Composable
private fun MainScreenFavoriteDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        MainScreen(state = MainUiState(MainTab.Favorite), onIntent = {}, pageContent = {})
    }
}
