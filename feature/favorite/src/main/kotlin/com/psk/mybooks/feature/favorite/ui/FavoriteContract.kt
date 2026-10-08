package com.psk.mybooks.feature.favorite.ui

import com.psk.mybooks.core.domain.model.Book

internal data class FavoriteUiState(val books: List<Book> = emptyList())

internal sealed interface FavoriteIntent {
    /** 하트를 눌러 즐겨찾기에서 뺌 */
    data class RemoveFavorite(val bookId: String) : FavoriteIntent
}
