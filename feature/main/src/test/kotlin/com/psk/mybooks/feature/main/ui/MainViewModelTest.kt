package com.psk.mybooks.feature.main.ui

import androidx.lifecycle.SavedStateHandle
import com.psk.mybooks.core.data.ConnectivityRepository
import com.psk.mybooks.core.domain.IsOnlineUseCase
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookUiModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    private class FakeConnectivityRepository(
        private val online: Boolean
    ) : ConnectivityRepository {
        override fun isOnline(): Boolean = online
    }

    private fun viewModel(online: Boolean = true, handle: SavedStateHandle = SavedStateHandle()): MainViewModel =
        MainViewModel(handle, IsOnlineUseCase(FakeConnectivityRepository(online)))

    @Test
    fun `바꾼 화면 조건이 저장된 상태로 새 ViewModel 에서 되살아난다`() {
        val handle: SavedStateHandle = SavedStateHandle()
        val first: MainViewModel = viewModel(handle = handle)
        first.onIntent(MainIntent.SelectTab(MainTab.Favorite))
        first.onIntent(MainIntent.ChangeSearchText("소설"))
        first.onIntent(MainIntent.ChangeSearchSort(SearchSortType.Latest))
        first.onIntent(MainIntent.ChangeFavoriteSort(FavoriteSortType.PriceDesc))
        first.onIntent(MainIntent.ChangeFavoritePriceFilter(PriceFilter(min = 1000, max = 30000)))
        first.onIntent(MainIntent.ChangeViewMode(ViewMode.Grid))

        // 프로세스가 종료됐다가 돌아온 것처럼, 저장된 handle 로 새 ViewModel 을 만든다
        val second: MainViewModel = viewModel(handle = handle)

        assertEquals(first.state.value, second.state.value)
        assertEquals(MainTab.Favorite, second.state.value.selectedTab)
        assertEquals("소설", second.state.value.searchText)
    }

    @Test
    fun `저장된 상태에서 돌아올 때는 인터넷이 없어도 안내하며 즐겨찾기로 옮기지 않는다`() = runTest {
        val handle: SavedStateHandle = SavedStateHandle()
        viewModel(online = true, handle = handle)
            .onIntent(MainIntent.SelectTab(MainTab.Search))

        val restored: MainViewModel = viewModel(online = false, handle = handle)

        assertEquals(MainTab.Search, restored.state.value.selectedTab)
        assertNull(
            withTimeoutOrNull(100) {
                restored.effect
                    .first()
            }
        )
    }

    @Test
    fun `읽을 수 없는 저장 값은 무시하고 기본 상태로 시작한다`() {
        val handle: SavedStateHandle = SavedStateHandle(mapOf(MAIN_SAVED_STATE_KEY to "{깨진 값"))

        val viewModel: MainViewModel = viewModel(handle = handle)

        assertEquals(MainUiState(), viewModel.state.value)
    }

    @Test
    fun `인터넷이 없이 열면 즐겨찾기 탭에서 시작하고 안내 Effect 를 보낸다`() = runTest {
        val viewModel: MainViewModel = viewModel(online = false)

        assertEquals(MainTab.Favorite, viewModel.state.value.selectedTab)
        assertEquals(
            MainEffect.MovedToFavoriteOffline,
            viewModel.effect
                .first()
        )
    }

    @Test
    fun `인터넷이 없어도 검색 탭으로 돌아갈 수 있다`() {
        val viewModel: MainViewModel = viewModel(online = false)

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Search))

        assertEquals(MainTab.Search, viewModel.state.value.selectedTab)
    }

    @Test
    fun `인터넷이 있으면 안내 없이 검색 탭에서 시작한다`() = runTest {
        val viewModel: MainViewModel = viewModel(online = true)

        assertEquals(MainTab.Search, viewModel.state.value.selectedTab)
        assertNull(
            withTimeoutOrNull(100) {
                viewModel.effect
                    .first()
            }
        )
    }

    @Test
    fun `처음에는 검색 탭이 선택되어 있다`() {
        val viewModel: MainViewModel = viewModel()

        assertEquals(MainTab.Search, viewModel.state.value.selectedTab)
    }

    @Test
    fun `SelectTab Intent 를 보내면 선택된 탭이 바뀐다`() {
        val viewModel: MainViewModel = viewModel()

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Favorite))

        assertEquals(MainTab.Favorite, viewModel.state.value.selectedTab)
    }

    @Test
    fun `같은 탭을 다시 선택해도 상태는 그대로다`() {
        val viewModel: MainViewModel = viewModel()

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Search))

        assertEquals(MainUiState(selectedTab = MainTab.Search), viewModel.state.value)
    }

    @Test
    fun `검색창 입력은 Intent 로만 상태에 반영된다`() {
        val viewModel: MainViewModel = viewModel()

        viewModel.onIntent(MainIntent.ChangeSearchText("해리 포터"))

        assertEquals("해리 포터", viewModel.state.value.searchText)
    }

    @Test
    fun `탭을 바꿔도 검색어는 유지된다`() {
        val viewModel: MainViewModel = viewModel()
        viewModel.onIntent(MainIntent.ChangeSearchText("해리 포터"))

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Favorite))

        assertEquals("해리 포터", viewModel.state.value.searchText)
    }

    @Test
    fun `처음 정렬 기준은 검색은 정확도순, 즐겨찾기는 제목 오름차순이다`() {
        val viewModel: MainViewModel = viewModel()

        assertEquals(SearchSortType.Accuracy, viewModel.state.value.searchSort)
        assertEquals(FavoriteSortType.TitleAsc, viewModel.state.value.favoriteSort)
    }

    @Test
    fun `검색 정렬과 즐겨찾기 정렬은 서로 영향을 주지 않는다`() {
        val viewModel: MainViewModel = viewModel()

        viewModel.onIntent(MainIntent.ChangeSearchSort(SearchSortType.Latest))
        assertEquals(SearchSortType.Latest, viewModel.state.value.searchSort)
        assertEquals(FavoriteSortType.TitleAsc, viewModel.state.value.favoriteSort)

        viewModel.onIntent(MainIntent.ChangeFavoriteSort(FavoriteSortType.PriceDesc))
        assertEquals(FavoriteSortType.PriceDesc, viewModel.state.value.favoriteSort)
        assertEquals(SearchSortType.Latest, viewModel.state.value.searchSort)
    }

    @Test
    fun `정렬을 바꿔도 검색어와 탭은 그대로다`() {
        val viewModel: MainViewModel = viewModel()
        viewModel.onIntent(MainIntent.ChangeSearchText("해리 포터"))
        viewModel.onIntent(MainIntent.SelectTab(MainTab.Favorite))

        viewModel.onIntent(MainIntent.ChangeFavoriteSort(FavoriteSortType.PriceAsc))

        assertEquals(MainTab.Favorite, viewModel.state.value.selectedTab)
        assertEquals("해리 포터", viewModel.state.value.searchText)
        assertEquals(FavoriteSortType.PriceAsc, viewModel.state.value.favoriteSort)
    }

    @Test
    fun `즐겨찾기 금액 필터는 기본으로 없고 바꾸거나 없앨 수 있다`() {
        val viewModel: MainViewModel = viewModel()
        assertEquals(PriceFilter(), viewModel.state.value.favoritePriceFilter)

        viewModel.onIntent(MainIntent.ChangeFavoritePriceFilter(PriceFilter(min = 1000, max = 20000)))
        assertEquals(PriceFilter(min = 1000, max = 20000), viewModel.state.value.favoritePriceFilter)

        viewModel.onIntent(MainIntent.ChangeFavoritePriceFilter(PriceFilter()))
        assertEquals(PriceFilter(), viewModel.state.value.favoritePriceFilter)
    }

    @Test
    fun `기본 보기 방식은 목록이고 토글에는 목록, 격자 순서로 나온다`() {
        val viewModel: MainViewModel = viewModel()

        assertEquals(ViewMode.List, viewModel.state.value.viewMode)
        assertEquals(listOf(ViewMode.List, ViewMode.Grid), ViewMode.entries)
    }

    @Test
    fun `ChangeViewMode Intent 를 보내면 보기 방식이 바뀐다`() {
        val viewModel: MainViewModel = viewModel()

        viewModel.onIntent(MainIntent.ChangeViewMode(ViewMode.Grid))

        assertEquals(ViewMode.Grid, viewModel.state.value.viewMode)
    }

    @Test
    fun `도서를 고르면 옆 패널에 보일 도서가 정해지고 처음에는 없다`() {
        val viewModel: MainViewModel = viewModel()
        assertNull(viewModel.state.value.selectedBook)
        val book: BookUiModel = BookUiModel(
            id = "1",
            status = "",
            title = "제목",
            contents = "",
            url = "",
            isbn = "",
            publishedAt = "",
            publisher = "",
            authors = "",
            translators = "",
            price = 0,
            salePrice = null,
            thumbnailUrl = ""
        )

        viewModel.onIntent(MainIntent.SelectBook(book))

        assertEquals(book, viewModel.state.value.selectedBook)
    }
}
