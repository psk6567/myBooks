package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookUiModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 프로세스가 종료됐다가(메모리 부족 등) 돌아올 때 화면 조건을 되살리기 위해 저장하는 값.
 * ViewModel 은 프로세스와 함께 사라지지만 `SavedStateHandle` 에 담은 값은 시스템이 보관했다가 돌려준다.
 * 저장 공간이 작아 문자열(JSON) 하나로 담고, enum 은 이름으로 저장한다. (이름이 바뀐 옛 값은 기본값으로 읽는다)
 */
@Serializable
internal data class MainSavedState(
    val tab: String,
    val searchText: String,
    val searchSort: String,
    val favoriteSort: String,
    val priceMin: Int?,
    val priceMax: Int?,
    val viewMode: String,
    val selectedBook: BookUiModel?
)

internal const val MAIN_SAVED_STATE_KEY: String = "main_ui_state"

internal fun MainUiState.toSavedJson(): String = Json.encodeToString(
    MainSavedState(
        tab = selectedTab.name,
        searchText = searchText,
        searchSort = searchSort.name,
        favoriteSort = favoriteSort.name,
        priceMin = favoritePriceFilter.min,
        priceMax = favoritePriceFilter.max,
        viewMode = viewMode.name,
        selectedBook = selectedBook
    )
)

/** 저장된 JSON 으로 상태를 되살린다. 없거나 읽을 수 없으면(형식이 바뀐 옛 값 등) null 이라 기본 상태로 시작한다. */
internal fun mainUiStateFromSaved(json: String?): MainUiState? {
    if (json == null) return null
    val saved: MainSavedState = try {
        Json.decodeFromString<MainSavedState>(json)
    } catch (e: IllegalArgumentException) {
        return null
    }
    val default: MainUiState = MainUiState()
    return MainUiState(
        selectedTab = MainTab.entries
            .firstOrNull { it.name == saved.tab } ?: default.selectedTab,
        searchText = saved.searchText,
        searchSort = SearchSortType.entries
            .firstOrNull { it.name == saved.searchSort } ?: default.searchSort,
        favoriteSort = FavoriteSortType.entries
            .firstOrNull { it.name == saved.favoriteSort } ?: default.favoriteSort,
        favoritePriceFilter = PriceFilter(min = saved.priceMin, max = saved.priceMax),
        viewMode = ViewMode.entries
            .firstOrNull { it.name == saved.viewMode } ?: default.viewMode,
        selectedBook = saved.selectedBook
    )
}
