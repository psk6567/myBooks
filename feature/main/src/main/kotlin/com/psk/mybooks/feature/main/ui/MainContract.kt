package com.psk.mybooks.feature.main.ui

import androidx.annotation.StringRes
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.feature.main.R

/** 하단 탭 순서. Pager 의 페이지 순서와 같다. */
internal enum class MainTab(
    @StringRes val labelRes: Int
) {
    Search(R.string.feature_main_tab_search),
    Favorite(R.string.feature_main_tab_favorite)
}

/** 정렬 기준의 화면 표시용 라벨 */
@StringRes
internal fun SearchSortType.labelRes(): Int = when (this) {
    SearchSortType.Accuracy -> R.string.feature_main_sort_accuracy
    SearchSortType.Latest -> R.string.feature_main_sort_latest
}

@StringRes
internal fun FavoriteSortType.labelRes(): Int = when (this) {
    FavoriteSortType.TitleAsc -> R.string.feature_main_sort_title_asc
    FavoriteSortType.TitleDesc -> R.string.feature_main_sort_title_desc
    FavoriteSortType.PriceAsc -> R.string.feature_main_sort_price_asc
    FavoriteSortType.PriceDesc -> R.string.feature_main_sort_price_desc
}

internal data class MainUiState(
    val selectedTab: MainTab = MainTab.Search,
    val searchText: String = "",
    /** 정렬은 탭마다 따로 가진다. 검색은 서버가, 즐겨찾기는 앱이 정렬해서 고를 수 있는 항목이 다르다. */
    val searchSort: SearchSortType = SearchSortType.Accuracy,
    val favoriteSort: FavoriteSortType = FavoriteSortType.TitleAsc,
    /** 금액 필터는 즐겨찾기 탭에만 있다. 검색은 서버가 결과를 주므로 앱에서 거르지 않는다. */
    val favoritePriceFilter: PriceFilter = PriceFilter(),
    val viewMode: ViewMode = ViewMode.List
)

internal sealed interface MainIntent {
    /** 탭을 누르거나 Pager 를 넘겨서 탭이 바뀜 */
    data class SelectTab(
        val tab: MainTab
    ) : MainIntent

    /** 검색창 입력이 바뀜 */
    data class ChangeSearchText(
        val text: String
    ) : MainIntent

    /** 검색 정렬 기준이 바뀜 */
    data class ChangeSearchSort(
        val sort: SearchSortType
    ) : MainIntent

    /** 즐겨찾기 정렬 기준이 바뀜 */
    data class ChangeFavoriteSort(
        val sort: FavoriteSortType
    ) : MainIntent

    /** 즐겨찾기 금액 필터가 바뀜. 필터를 없애려면 빈 [PriceFilter] 를 보낸다. */
    data class ChangeFavoritePriceFilter(
        val priceFilter: PriceFilter
    ) : MainIntent

    /** 격자/목록 보기 방식이 바뀜 */
    data class ChangeViewMode(
        val viewMode: ViewMode
    ) : MainIntent
}
