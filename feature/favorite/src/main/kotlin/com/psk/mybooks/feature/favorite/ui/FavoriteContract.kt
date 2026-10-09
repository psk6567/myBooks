package com.psk.mybooks.feature.favorite.ui

import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType

/**
 * @param sort 정렬 기준. main 의 칩 값을 그대로 받아 둔다.
 */
internal data class FavoriteUiState(
    val books: List<Book> = emptyList(),
    val sort: FavoriteSortType = FavoriteSortType.TitleAsc
)

internal sealed interface FavoriteIntent {
    /** main 에서 정렬이 바뀜 */
    data class ChangeSort(
        val sort: FavoriteSortType
    ) : FavoriteIntent

    /** 하트를 눌러 즐겨찾기에서 뺌 */
    data class RemoveFavorite(
        val bookId: String
    ) : FavoriteIntent
}
