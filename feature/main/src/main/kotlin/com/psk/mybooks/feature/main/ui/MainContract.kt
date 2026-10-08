package com.psk.mybooks.feature.main.ui

import androidx.annotation.StringRes
import com.psk.mybooks.core.model.SortType
import com.psk.mybooks.core.model.ViewMode
import com.psk.mybooks.feature.main.R

/** 하단 탭 순서. Pager 의 페이지 순서와 같다. */
internal enum class MainTab(@StringRes val labelRes: Int) {
    Search(R.string.feature_main_tab_search),
    Favorite(R.string.feature_main_tab_favorite)
}

/** 정렬 기준의 화면 표시용 라벨 */
@StringRes
internal fun SortType.labelRes(): Int = when (this) {
    SortType.TitleAsc -> R.string.feature_main_sort_title_asc
    SortType.TitleDesc -> R.string.feature_main_sort_title_desc
    SortType.PriceAsc -> R.string.feature_main_sort_price_asc
    SortType.PriceDesc -> R.string.feature_main_sort_price_desc
}

internal data class MainUiState(
    val selectedTab: MainTab = MainTab.Search,
    val searchText: String = "",
    val sort: SortType = SortType.TitleAsc,
    val viewMode: ViewMode = ViewMode.List
)

internal sealed interface MainIntent {
    /** 탭을 누르거나 Pager 를 넘겨서 탭이 바뀜 */
    data class SelectTab(val tab: MainTab) : MainIntent

    /** 검색창 입력이 바뀜 */
    data class ChangeSearchText(val text: String) : MainIntent

    /** 정렬 기준이 바뀜 */
    data class ChangeSort(val sort: SortType) : MainIntent

    /** 격자/목록 보기 방식이 바뀜 */
    data class ChangeViewMode(val viewMode: ViewMode) : MainIntent
}
