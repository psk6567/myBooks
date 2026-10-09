package com.psk.mybooks.feature.favorite.ui

import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.ui.BookUiModel

/**
 * @param sort 정렬 기준. main 의 칩 값을 그대로 받아 둔다.
 * @param priceFilter 금액 필터. main 의 칩 값을 그대로 받아 둔다.
 */
internal data class FavoriteUiState(
    val books: List<BookUiModel> = emptyList(),
    val sort: FavoriteSortType = FavoriteSortType.TitleAsc,
    val priceFilter: PriceFilter = PriceFilter()
)

/** 한 번만 처리할 일 */
internal sealed interface FavoriteEffect {
    /** 상세 화면으로 이동 */
    data class OpenDetail(
        val book: BookUiModel
    ) : FavoriteEffect
}

internal sealed interface FavoriteIntent {
    /** 목록의 도서를 눌러 상세 화면을 열려고 함 */
    data class OpenDetail(
        val book: BookUiModel
    ) : FavoriteIntent

    /** main 에서 정렬이나 금액 필터가 바뀜 */
    data class ChangeCondition(
        val sort: FavoriteSortType,
        val priceFilter: PriceFilter
    ) : FavoriteIntent

    /** 하트를 눌러 즐겨찾기에서 뺌 */
    data class RemoveFavorite(
        val bookId: String
    ) : FavoriteIntent
}
