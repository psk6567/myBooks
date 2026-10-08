package com.psk.mybooks.feature.search.ui

import com.psk.mybooks.core.model.Book

internal data class SearchUiState(
    val books: List<Book> = emptyList(),
    /** 즐겨찾기한 도서의 id */
    val favoriteIds: Set<String> = emptySet()
)

internal sealed interface SearchIntent {
    /** 하트를 눌러 즐겨찾기를 켜거나 끔 */
    data class ToggleFavorite(val bookId: String) : SearchIntent
}
