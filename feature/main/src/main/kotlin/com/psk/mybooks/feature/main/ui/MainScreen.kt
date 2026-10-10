package com.psk.mybooks.feature.main.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.util.Consumer
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.psk.mybooks.core.designsystem.component.BottomSheetHandle
import com.psk.mybooks.core.designsystem.component.EmptyMessage
import com.psk.mybooks.core.designsystem.component.Icon
import com.psk.mybooks.core.designsystem.component.LocalSnackbar
import com.psk.mybooks.core.designsystem.component.MbSnackbarHost
import com.psk.mybooks.core.designsystem.component.OptionItem
import com.psk.mybooks.core.designsystem.component.OutlinedChip
import com.psk.mybooks.core.designsystem.component.SearchBar
import com.psk.mybooks.core.designsystem.component.SnackbarHandle
import com.psk.mybooks.core.designsystem.component.SnackbarProvider
import com.psk.mybooks.core.designsystem.component.TabBar
import com.psk.mybooks.core.designsystem.component.TabItem
import com.psk.mybooks.core.designsystem.component.TabRail
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.component.rememberBottomSheet
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer
import com.psk.mybooks.core.designsystem.util.isWideScreen
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.feature.detail.navigation.detailScreen
import com.psk.mybooks.feature.detail.navigation.navigateToDetail
import com.psk.mybooks.feature.detail.ui.DetailPane
import com.psk.mybooks.feature.favorite.ui.FavoriteRoute
import com.psk.mybooks.feature.main.R
import com.psk.mybooks.feature.search.ui.SearchRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable

/** 화면 전환(옆으로 밀기)에 걸리는 시간 */
private const val NAV_ANIMATION_MILLIS: Int = 300

/** 가려지는 화면이 밀려나는 거리. 화면 폭의 1/NAV_PARALLAX 만큼만 움직여서 새 화면이 위로 덮는 느낌을 준다. */
private const val NAV_PARALLAX: Int = 4

/** 넓은 화면에서 목록 영역에 대한 상세 패널의 폭 비율 (목록 1 : 상세 1.3) */
private const val DETAIL_PANE_WEIGHT: Float = 1.3f

/** 홈(탭 화면)의 이동 경로 */
@Serializable
private data object HomeDestination

/**
 * 앱의 화면 이동을 맡는 진입점. 홈(검색·즐겨찾기 탭)에서 도서를 누르면 상세 화면으로 이동한다.
 * 화면끼리는 서로를 모르므로(feature 는 다른 feature 를 의존하지 않는다) 이동은 main 이 연결한다.
 */
@Composable
fun MainRoute(modifier: Modifier = Modifier, onTitleClick: (() -> Unit)? = null) {
    val navController: NavHostController = rememberNavController()

    // 앱이 이미 떠 있는 상태에서 앱 스킴 링크가 오면 Activity 는 새로 만들어지지 않고 새 Intent 만 받는다.
    // NavHost 는 이것을 알아서 처리하지 않으므로 직접 받아 handleDeepLink 로 넘긴다.
    // TODO: 아직 등록된 딥링크가 없어 아무 일도 하지 않는다. (DetailNavigation 의 TODO 참고)
    val activity: ComponentActivity? = LocalActivity.current as? ComponentActivity
    DisposableEffect(activity, navController) {
        val listener: Consumer<Intent> = Consumer { intent -> navController.handleDeepLink(intent) }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }

    NavHost(
        navController = navController,
        startDestination = HomeDestination,
        modifier = modifier,
        // 화면 전환은 옆으로 미는 방식이다. 상세로 갈 때는 새 화면이 오른쪽에서 들어오고 이전 화면은 왼쪽으로 조금 밀려나며,
        // 뒤로 갈 때는 반대로 움직인다. (기본 전환은 페이드라 이동 방향이 느껴지지 않는다)
        enterTransition = { slideIntoContainer(SlideDirection.Start, tween(NAV_ANIMATION_MILLIS)) },
        exitTransition = {
            slideOutOfContainer(SlideDirection.Start, tween(NAV_ANIMATION_MILLIS), targetOffset = { it / NAV_PARALLAX })
        },
        popEnterTransition = {
            slideIntoContainer(SlideDirection.End, tween(NAV_ANIMATION_MILLIS), initialOffset = { it / NAV_PARALLAX })
        },
        popExitTransition = { slideOutOfContainer(SlideDirection.End, tween(NAV_ANIMATION_MILLIS)) }
    ) {
        composable<HomeDestination> {
            MainHome(onNavigateToDetail = navController::navigateToDetail, onTitleClick = onTitleClick)
        }
        detailScreen(onBack = { navController.popBackStack() })
    }
}

/**
 * 홈 화면. ViewModel 을 연결한다.
 * 도서를 누르면 좁은 화면에서는 상세 화면으로 이동하고, 넓은 화면에서는 이동 없이 목록 옆 패널에 상세를 보여 준다.
 */
@Composable
private fun MainHome(onNavigateToDetail: (BookUiModel) -> Unit, onTitleClick: (() -> Unit)?) {
    val viewModel: MainViewModel = hiltViewModel()
    val state: MainUiState by viewModel.state
        .collectAsStateWithLifecycle()
    val isWide: Boolean = isWideScreen()
    // 넓은 화면에서만 옆 패널에 열린 도서를 목록에서 강조한다. (좁은 화면은 상세가 별도 화면이라 강조할 것이 없다)
    val selectedBookId: String? = if (isWide) {
        state.selectedBook
            ?.id
    } else {
        null
    }
    val onBookClick: (BookUiModel) -> Unit = { book ->
        if (isWide) viewModel.onIntent(MainIntent.SelectBook(book)) else onNavigateToDetail(book)
    }

    MainScreen(
        state = state,
        onIntent = viewModel::onIntent,
        effect = viewModel.effect,
        onTitleClick = onTitleClick,
        detailContent = { book -> DetailPane(book = book) },
        pageContent = { tab ->
            when (tab) {
                MainTab.Search -> SearchRoute(
                    query = state.searchText,
                    sort = state.searchSort,
                    isGrid = state.viewMode == ViewMode.Grid,
                    onBookClick = onBookClick,
                    selectedBookId = selectedBookId
                )

                MainTab.Favorite -> FavoriteRoute(
                    sort = state.favoriteSort,
                    priceFilter = state.favoritePriceFilter,
                    query = state.searchText,
                    isGrid = state.viewMode == ViewMode.Grid,
                    onBookClick = onBookClick,
                    selectedBookId = selectedBookId
                )
            }
        }
    )
}

/** [MainEffect] 를 받아 스낵바로 알린다. [SnackbarProvider] 안에서 불러야 한다. */
@Composable
private fun MainEffectHandler(effect: Flow<MainEffect>) {
    val snackbar: SnackbarHandle = LocalSnackbar.current
    val offlineMessage: String = stringResource(R.string.feature_main_offline_moved_to_favorite)
    LaunchedEffect(effect) {
        effect.collect { event ->
            when (event) {
                MainEffect.MovedToFavoriteOffline -> snackbar.show(offlineMessage)
            }
        }
    }
}

/**
 * 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상.
 *
 * @param pageContent 탭별 화면. 다른 feature 의 ViewModel 을 직접 만들지 않도록 호출하는 쪽에서 넘긴다.
 * @param detailContent 넓은 화면에서 목록 옆 패널에 보일 상세. 선택한 도서를 받는다.
 * @param effect 한 번만 처리할 일(오프라인 안내 등). 스낵바 호스트가 이 화면에 있어서 여기서 받는다.
 */
@Composable
internal fun MainScreen(
    state: MainUiState,
    onIntent: (MainIntent) -> Unit,
    pageContent: @Composable (MainTab) -> Unit,
    detailContent: @Composable (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
    effect: Flow<MainEffect> = emptyFlow(),
    onTitleClick: (() -> Unit)? = null
) {
    val pagerState: PagerState = rememberPagerState(initialPage = state.selectedTab.ordinal) { MainTab.entries.size }
    val currentOnIntent: (MainIntent) -> Unit by rememberUpdatedState(onIntent)
    // 열림 여부는 UI 가 관리하고(ViewModel 은 선택 결과 Intent 만 받는다), 열린 동안 바뀐 정렬도 바로 반영된다
    val sortSheet: BottomSheetHandle = rememberBottomSheet { dismiss ->
        when (state.selectedTab) {
            MainTab.Search -> SearchSortType.entries.forEach { sort ->
                OptionItem(
                    text = stringResource(sort.labelRes()),
                    selected = sort == state.searchSort,
                    onClick = {
                        onIntent(MainIntent.ChangeSearchSort(sort))
                        dismiss()
                    }
                )
            }

            MainTab.Favorite -> FavoriteSortType.entries.forEach { sort ->
                OptionItem(
                    text = stringResource(sort.labelRes()),
                    selected = sort == state.favoriteSort,
                    onClick = {
                        onIntent(MainIntent.ChangeFavoriteSort(sort))
                        dismiss()
                    }
                )
            }
        }
    }

    val priceFilterSheet: BottomSheetHandle = rememberBottomSheet { dismiss ->
        PriceFilterSheet(
            filter = state.favoritePriceFilter,
            onApply = { priceFilter ->
                onIntent(MainIntent.ChangeFavoritePriceFilter(priceFilter))
                dismiss()
            }
        )
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

    // 하위 화면(검색, 즐겨찾기 등)이 LocalSnackbar 로 스낵바를 띄울 수 있게 호스트를 여기서 한 번 둔다
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    // 넓은 화면(태블릿, 펼친 폴더블)에서는 아래 탭바 대신 왼쪽 탭 레일을 쓴다
    val isWide: Boolean = isWideScreen()
    val tabItems: List<TabItem> = rememberTabItems()
    // 키보드가 올라와 있는지. 올라와 있으면 아래 탭바는 키보드 뒤로 가려지므로 그리지 않는다.
    val isKeyboardVisible: Boolean = WindowInsets.ime
        .getBottom(LocalDensity.current) > 0

    SnackbarProvider(snackbarHostState) {
        MainEffectHandler(effect)
        Scaffold(
            modifier = modifier,
            containerColor = MyBooksTheme.colors.background,
            snackbarHost = { MbSnackbarHost(snackbarHostState) },
            topBar = {
                // Scaffold 는 topBar 에 좌우 여백을 주지 않으므로 가로 모드의 내비게이션 바를 피한다
                Column(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Horizontal)
                    )
                ) {
                    TitleBar(
                        label = stringResource(state.selectedTab.labelRes),
                        // 제목을 여러 번 누르면 여는 숨은 입구(debug 빌드의 개발자 모드). 없으면 누를 수 있는 영역이 되지 않는다.
                        modifier = if (onTitleClick == null) {
                            Modifier
                        } else {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onTitleClick
                            )
                        }
                    )
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
                if (!isWide && !isKeyboardVisible) {
                    TabBar(
                        tabs = tabItems,
                        selectedIndex = state.selectedTab.ordinal,
                        onTabSelected = { onIntent(MainIntent.SelectTab(MainTab.entries[it])) }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .padding(innerPadding)
                    // Scaffold 가 이미 준 여백(내비게이션 바 등)은 쓴 것으로 표시해서, 키보드 여백이 그만큼을 겹쳐 더하지 않게 한다.
                    // 키보드가 올라오면 목록 영역이 그 높이만큼 줄어서 마지막 항목까지 키보드 위에서 볼 수 있다.
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .fillMaxSize()
            ) {
                if (isWide) {
                    TabRail(
                        tabs = tabItems,
                        selectedIndex = state.selectedTab.ordinal,
                        onTabSelected = { onIntent(MainIntent.SelectTab(MainTab.entries[it])) }
                    )
                }
                // 목록 영역. 넓은 화면에서는 오른쪽에 상세 패널이 붙으므로 남는 폭을 나눠 쓴다.
                // 페이지 안 목록의 스크롤이 nestedScroll 로 올라와 정렬·보기 방식 줄을 숨기고 보인다
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .nestedScroll(hideOnScrollState.connection)
                ) {
                    Column(modifier = Modifier.hideOnScroll(hideOnScrollState)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedChip(
                                    text = stringResource(
                                        when (state.selectedTab) {
                                            MainTab.Search ->
                                                state.searchSort
                                                    .labelRes()

                                            MainTab.Favorite ->
                                                state.favoriteSort
                                                    .labelRes()
                                        }
                                    ),
                                    onClick = sortSheet::show
                                )
                                // 금액 필터는 앱에서 거르는 즐겨찾기에만 있다
                                if (state.selectedTab == MainTab.Favorite) {
                                    OutlinedChip(
                                        text = priceFilterLabel(state.favoritePriceFilter),
                                        onClick = priceFilterSheet::show,
                                        selected = state.favoritePriceFilter.isActive
                                    )
                                }
                            }
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        // 탭이 둘뿐이라 둘 다 유지해서 탭을 오가도 스크롤·입력 상태가 남게 한다
                        beyondViewportPageCount = MainTab.entries.size - 1
                    ) { page ->
                        pageContent(MainTab.entries[page])
                    }
                }
                if (isWide) {
                    VerticalDivider(color = MyBooksTheme.colors.divider)
                    Box(
                        modifier = Modifier
                            .weight(DETAIL_PANE_WEIGHT)
                            .fillMaxHeight()
                    ) {
                        val selectedBook: BookUiModel? = state.selectedBook
                        if (selectedBook == null) {
                            EmptyMessage(message = stringResource(R.string.feature_main_select_book))
                        } else {
                            detailContent(selectedBook)
                        }
                    }
                }
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
        MainScreen(state = MainUiState(MainTab.Search), onIntent = {}, pageContent = {}, detailContent = {})
    }
}

@Preview(name = "Favorite tab (Dark)", showSystemUi = true)
@Composable
private fun MainScreenFavoriteDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        MainScreen(state = MainUiState(MainTab.Favorite, searchText = "해리 포터"), onIntent = {
        }, pageContent = {}, detailContent = {})
    }
}
