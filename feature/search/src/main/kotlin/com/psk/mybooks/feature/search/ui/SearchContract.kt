package com.psk.mybooks.feature.search.ui

import com.psk.mybooks.core.domain.model.SearchSortType

/**
 * 검색 조건과 화면 상태. 도서 목록은 상태가 아니라 [SearchViewModel.books] 흐름(Paging)으로 나온다.
 *
 * @param query 검색어. main 의 검색창이 가진 값을 그대로 받아 둔다.
 */
internal data class SearchUiState(
    val query: String = "",
    val sort: SearchSortType = SearchSortType.Accuracy,
    /** 즐겨찾기한 도서의 id */
    val favoriteIds: Set<String> = emptySet()
)

internal sealed interface SearchIntent {
    /** main 에서 검색어나 정렬이 바뀜 */
    data class ChangeCondition(val query: String, val sort: SearchSortType) : SearchIntent

    /** 하트를 눌러 즐겨찾기를 켜거나 끔 */
    data class ToggleFavorite(val bookId: String) : SearchIntent
}
