package com.psk.mybooks.feature.search.ui

import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType

/**
 * 검색 조건과 화면 상태. 도서 목록은 상태가 아니라 [SearchViewModel.books] 흐름(Paging)으로 나온다.
 *
 * @param query 검색어. main 의 검색창이 가진 값을 그대로 받아 둔다.
 */
internal data class SearchUiState(
    val query: String = "",
    val sort: SearchSortType = SearchSortType.Accuracy,
    /** 즐겨찾기한 도서의 id. 어디서든 담거나 빼면 바로 바뀐다. */
    val favoriteIds: Set<String> = emptySet()
)

internal sealed interface SearchIntent {
    /** main 에서 검색어나 정렬이 바뀜 */
    data class ChangeCondition(
        val query: String,
        val sort: SearchSortType
    ) : SearchIntent

    /** 검색 결과를 불러오다 실패함. 화면의 로드 상태에서 읽어 알려 준다. 같은 오류 객체는 한 번만 전달된다. ([com.psk.mybooks.core.common.MviViewModel.sendError] 참고) */
    data class ReportLoadError(
        val error: Throwable
    ) : SearchIntent

    /** 하트를 눌러 즐겨찾기를 켜거나 끔. 켤 때 저장하므로 도서 전체를 받는다. */
    data class ToggleFavorite(
        val book: Book
    ) : SearchIntent
}
