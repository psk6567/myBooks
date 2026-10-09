package com.psk.mybooks.feature.favorite.ui

import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.ui.BookUiModel

/**
 * @param sort 정렬 기준. main 의 칩 값을 그대로 받아 둔다.
 * @param priceFilter 금액 필터. main 의 칩 값을 그대로 받아 둔다.
 * @param pendingRemoval 삭제 확인을 기다리는 도서. 있으면 "삭제할까요?" 다이얼로그가 보인다.
 * 어떤 도서인지가 필요해서 UI 가 아니라 여기에 두며, 그래서 화면을 회전해도 유지된다.
 */
internal data class FavoriteUiState(
    val books: List<BookUiModel> = emptyList(),
    val sort: FavoriteSortType = FavoriteSortType.TitleAsc,
    val priceFilter: PriceFilter = PriceFilter(),
    val pendingRemoval: BookUiModel? = null
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

    /** 하트를 눌러 즐겨찾기에서 빼려고 함. 바로 빼지 않고 삭제 확인 다이얼로그를 띄운다. */
    data class RequestRemove(
        val book: BookUiModel
    ) : FavoriteIntent

    /** 삭제 확인 다이얼로그에서 삭제를 누름 */
    data object ConfirmRemove : FavoriteIntent

    /** 삭제 확인 다이얼로그를 취소하거나 닫음 */
    data object DismissRemove : FavoriteIntent
}
