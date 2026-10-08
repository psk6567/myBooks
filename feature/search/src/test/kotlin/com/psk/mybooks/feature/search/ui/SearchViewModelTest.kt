package com.psk.mybooks.feature.search.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @Test
    fun `시작하면 도서 목록이 채워져 있다`() {
        val viewModel: SearchViewModel = SearchViewModel()

        assertTrue(viewModel.state.value.books.isNotEmpty())
    }

    @Test
    fun `하트를 누르면 즐겨찾기가 켜지고 다시 누르면 꺼진다`() {
        val viewModel: SearchViewModel = SearchViewModel()
        val bookId: String = viewModel.state.value.books.first().id

        viewModel.onIntent(SearchIntent.ToggleFavorite(bookId))
        assertEquals(setOf(bookId), viewModel.state.value.favoriteIds)

        viewModel.onIntent(SearchIntent.ToggleFavorite(bookId))
        assertEquals(emptySet<String>(), viewModel.state.value.favoriteIds)
    }

    @Test
    fun `도서 id 는 서로 겹치지 않는다`() {
        val ids: List<String> = SearchViewModel().state.value.books.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }
}
