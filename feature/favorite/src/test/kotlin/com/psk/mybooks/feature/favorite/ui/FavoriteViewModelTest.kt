package com.psk.mybooks.feature.favorite.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoriteViewModelTest {

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule()

    @Test
    fun `시작하면 즐겨찾기 목록이 채워져 있다`() {
        val viewModel: FavoriteViewModel = FavoriteViewModel()

        assertTrue(viewModel.state.value.books.isNotEmpty())
    }

    @Test
    fun `하트를 누르면 그 도서만 목록에서 빠진다`() {
        val viewModel: FavoriteViewModel = FavoriteViewModel()
        val before: List<String> = viewModel.state.value.books.map { it.id }

        viewModel.onIntent(FavoriteIntent.RemoveFavorite(before.first()))

        val after: List<String> = viewModel.state.value.books.map { it.id }
        assertFalse(before.first() in after)
        assertEquals(before.drop(1), after)
    }

    @Test
    fun `모두 빼면 목록이 비어 있다`() {
        val viewModel: FavoriteViewModel = FavoriteViewModel()

        viewModel.state.value.books.map { it.id }.forEach { viewModel.onIntent(FavoriteIntent.RemoveFavorite(it)) }

        assertTrue(viewModel.state.value.books.isEmpty())
    }
}
