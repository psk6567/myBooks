package com.psk.mybooks.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psk.mybooks.core.designsystem.component.BottomSheetHandle
import com.psk.mybooks.core.designsystem.component.Icon
import com.psk.mybooks.core.designsystem.component.OptionItem
import com.psk.mybooks.core.designsystem.component.OutlinedChip
import com.psk.mybooks.core.designsystem.component.SearchBar
import com.psk.mybooks.core.designsystem.component.TabBar
import com.psk.mybooks.core.designsystem.component.TabItem
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.component.rememberBottomSheet
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer
import com.psk.mybooks.core.model.SortType
import com.psk.mybooks.core.model.ViewMode
import com.psk.mybooks.feature.favorite.ui.FavoriteRoute
import com.psk.mybooks.feature.main.R
import com.psk.mybooks.feature.search.ui.SearchRoute

/** ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다. */
@Composable
fun MainRoute(modifier: Modifier = Modifier) {
    val viewModel: MainViewModel = viewModel()
    val state: MainUiState by viewModel.state.collectAsStateWithLifecycle()

    MainScreen(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        pageContent = { tab ->
            when (tab) {
                MainTab.Search -> SearchRoute(viewMode = state.viewMode)
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
    val pagerState: PagerState = rememberPagerState(initialPage = state.selectedTab.ordinal) { MainTab.entries.size }
    val currentOnIntent: (MainIntent) -> Unit by rememberUpdatedState(onIntent)
    // 열림 여부는 UI 가 관리하고(ViewModel 은 선택 결과 Intent 만 받는다), 열린 동안 바뀐 정렬도 바로 반영된다
    val sortSheet: BottomSheetHandle = rememberBottomSheet { dismiss ->
        SortType.entries.forEach { sort ->
            OptionItem(
                text = stringResource(sort.labelRes()),
                selected = sort == state.sort,
                onClick = {
                    onIntent(MainIntent.ChangeSort(sort))
                    dismiss()
                }
            )
        }
    }

    val hideOnScrollState: HideOnScrollState = remember { HideOnScrollState() }
    // 탭이나 검색어가 바뀌어 목록이 짧아지면 스크롤할 수 없어 줄이 숨겨진 채 남을 수 있으므로 다시 보인다
    LaunchedEffect(state.selectedTab, state.searchText) { hideOnScrollState.show() }

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
        topBar = {
            // Scaffold 는 topBar 에 좌우 여백을 주지 않으므로 가로 모드의 내비게이션 바를 피한다
            Column(
                modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            ) {
                TitleBar(label = stringResource(state.selectedTab.labelRes))
                SearchBar(
                    query = state.searchText,
                    onQueryChange = { onIntent(MainIntent.ChangeSearchText(it)) },
                    placeholder = stringResource(R.string.feature_main_search_placeholder),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                VSpacer(12.dp)
            }
        },
        bottomBar = {
            TabBar(
                tabs = rememberTabItems(),
                selectedIndex = state.selectedTab.ordinal,
                onTabSelected = { onIntent(MainIntent.SelectTab(MainTab.entries[it])) }
            )
        }
    ) { innerPadding ->
        // 페이지 안 목록의 스크롤이 nestedScroll 로 올라와 정렬·보기 방식 줄을 숨기고 보인다
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().nestedScroll(hideOnScrollState.connection)) {
            Column(modifier = Modifier.hideOnScroll(hideOnScrollState)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedChip(text = stringResource(state.sort.labelRes()), onClick = sortSheet::show)
                    // 격자/목록 상태만 바꾸고, 실제 화면 반영은 검색·즐겨찾기 화면을 만들 때 연결한다
                    ViewModeToggle(
                        selected = state.viewMode,
                        onSelect = { onIntent(MainIntent.ChangeViewMode(it)) }
                    )
                }
                VSpacer(12.dp)
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                // 탭이 둘뿐이라 둘 다 유지해서 탭을 오가도 스크롤·입력 상태가 남게 한다
                beyondViewportPageCount = MainTab.entries.size - 1
            ) { page ->
                pageContent(MainTab.entries[page])
            }
        }
    }
}

@Composable
private fun rememberTabItems(): List<TabItem> {
    val searchLabel: String = stringResource(MainTab.Search.labelRes)
    val favoriteLabel: String = stringResource(MainTab.Favorite.labelRes)

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
        MainScreen(state = MainUiState(MainTab.Favorite, searchText = "해리 포터"), onIntent = {}, pageContent = {})
    }
}
