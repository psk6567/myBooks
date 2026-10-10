package com.psk.mybooks.feature.search.ui

import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.model.SearchTargetType
import com.psk.mybooks.core.ui.BookUiModel

/**
 * 검색 조건과 화면 상태. 도서 목록은 상태가 아니라 [SearchViewModel.books] 흐름(Paging)으로 나온다.
 *
 * @param query 검색어. main 의 검색창이 가진 값을 그대로 받아 둔다.
 */
internal data class SearchUiState(
    val query: String = "",
    val sort: SearchSortType = SearchSortType.Accuracy,
    val target: SearchTargetType = SearchTargetType.All,
    /** 즐겨찾기한 도서의 id. 어디서든 담거나 빼면 바로 바뀐다. */
    val favoriteIds: Set<String> = emptySet()
)

/** 한 번만 처리할 일 */
internal sealed interface SearchEffect {
    /** 상세 화면으로 이동 */
    data class OpenDetail(
        val book: BookUiModel
    ) : SearchEffect
}

internal sealed interface SearchIntent {
    /** main 에서 검색어, 정렬, 검색 대상이 바뀜 */
    data class ChangeCondition(
        val query: String,
        val sort: SearchSortType,
        val target: SearchTargetType = SearchTargetType.All
    ) : SearchIntent

    /** 목록의 도서를 눌러 상세 화면을 열려고 함 */
    data class OpenDetail(
        val book: BookUiModel
    ) : SearchIntent

    /** 검색 결과를 불러오다 실패함. 화면의 로드 상태에서 읽어 알려 준다.
     * 목록 안에서 이미 안내하므로 스낵바로 또 알리지 않고, 사용자가 확인해야 하는 높은 중요도의 오류(인증·권한)만
     * 공통 오류 흐름으로 보낸다. 같은 오류 객체는 한 번만 전달된다. ([com.psk.mybooks.core.common.MviViewModel.sendError] 참고) */
    data class ReportLoadError(
        val error: Throwable
    ) : SearchIntent

    /** 하트를 눌러 즐겨찾기를 켜거나 끔. 저장할 도서 전체는 ViewModel 이 들고 있으므로 id 만 받는다. */
    data class ToggleFavorite(
        val bookId: String
    ) : SearchIntent
}
