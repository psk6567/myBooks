package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.model.SearchSortType
import org.junit.Assert.assertEquals
import org.junit.Test

class MainViewModelTest {

    @Test
    fun `처음에는 검색 탭이 선택되어 있다`() {
        val viewModel: MainViewModel = MainViewModel()

        assertEquals(MainTab.Search, viewModel.state.value.selectedTab)
    }

    @Test
    fun `SelectTab Intent 를 보내면 선택된 탭이 바뀐다`() {
        val viewModel: MainViewModel = MainViewModel()

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Favorite))

        assertEquals(MainTab.Favorite, viewModel.state.value.selectedTab)
    }

    @Test
    fun `같은 탭을 다시 선택해도 상태는 그대로다`() {
        val viewModel: MainViewModel = MainViewModel()

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Search))

        assertEquals(MainUiState(selectedTab = MainTab.Search), viewModel.state.value)
    }

    @Test
    fun `검색창 입력은 Intent 로만 상태에 반영된다`() {
        val viewModel: MainViewModel = MainViewModel()

        viewModel.onIntent(MainIntent.ChangeSearchText("해리 포터"))

        assertEquals("해리 포터", viewModel.state.value.searchText)
    }

    @Test
    fun `탭을 바꿔도 검색어는 유지된다`() {
        val viewModel: MainViewModel = MainViewModel()
        viewModel.onIntent(MainIntent.ChangeSearchText("해리 포터"))

        viewModel.onIntent(MainIntent.SelectTab(MainTab.Favorite))

        assertEquals("해리 포터", viewModel.state.value.searchText)
    }

    @Test
    fun `처음 정렬 기준은 검색은 정확도순, 즐겨찾기는 제목 오름차순이다`() {
        val viewModel: MainViewModel = MainViewModel()

        assertEquals(SearchSortType.Accuracy, viewModel.state.value.searchSort)
        assertEquals(FavoriteSortType.TitleAsc, viewModel.state.value.favoriteSort)
    }

    @Test
    fun `검색 정렬과 즐겨찾기 정렬은 서로 영향을 주지 않는다`() {
        val viewModel: MainViewModel = MainViewModel()

        viewModel.onIntent(MainIntent.ChangeSearchSort(SearchSortType.Latest))
        assertEquals(SearchSortType.Latest, viewModel.state.value.searchSort)
        assertEquals(FavoriteSortType.TitleAsc, viewModel.state.value.favoriteSort)

        viewModel.onIntent(MainIntent.ChangeFavoriteSort(FavoriteSortType.PriceDesc))
        assertEquals(FavoriteSortType.PriceDesc, viewModel.state.value.favoriteSort)
        assertEquals(SearchSortType.Latest, viewModel.state.value.searchSort)
    }

    @Test
    fun `정렬을 바꿔도 검색어와 탭은 그대로다`() {
        val viewModel: MainViewModel = MainViewModel()
        viewModel.onIntent(MainIntent.ChangeSearchText("해리 포터"))
        viewModel.onIntent(MainIntent.SelectTab(MainTab.Favorite))

        viewModel.onIntent(MainIntent.ChangeFavoriteSort(FavoriteSortType.PriceAsc))

        assertEquals(MainTab.Favorite, viewModel.state.value.selectedTab)
        assertEquals("해리 포터", viewModel.state.value.searchText)
        assertEquals(FavoriteSortType.PriceAsc, viewModel.state.value.favoriteSort)
    }

    @Test
    fun `즐겨찾기 금액 필터는 기본으로 없고 바꾸거나 없앨 수 있다`() {
        val viewModel: MainViewModel = MainViewModel()
        assertEquals(PriceFilter(), viewModel.state.value.favoritePriceFilter)

        viewModel.onIntent(MainIntent.ChangeFavoritePriceFilter(PriceFilter(min = 1000, max = 20000)))
        assertEquals(PriceFilter(min = 1000, max = 20000), viewModel.state.value.favoritePriceFilter)

        viewModel.onIntent(MainIntent.ChangeFavoritePriceFilter(PriceFilter()))
        assertEquals(PriceFilter(), viewModel.state.value.favoritePriceFilter)
    }

    @Test
    fun `기본 보기 방식은 목록이고 토글에는 목록, 격자 순서로 나온다`() {
        val viewModel: MainViewModel = MainViewModel()

        assertEquals(ViewMode.List, viewModel.state.value.viewMode)
        assertEquals(listOf(ViewMode.List, ViewMode.Grid), ViewMode.entries)
    }

    @Test
    fun `ChangeViewMode Intent 를 보내면 보기 방식이 바뀐다`() {
        val viewModel: MainViewModel = MainViewModel()

        viewModel.onIntent(MainIntent.ChangeViewMode(ViewMode.Grid))

        assertEquals(ViewMode.Grid, viewModel.state.value.viewMode)
    }
}
