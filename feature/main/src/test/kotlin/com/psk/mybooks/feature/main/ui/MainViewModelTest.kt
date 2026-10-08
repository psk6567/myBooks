package com.psk.mybooks.feature.main.ui

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

        assertEquals(MainUiState(MainTab.Search), viewModel.state.value)
    }
}
