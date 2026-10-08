package com.psk.mybooks.feature.main.ui

import androidx.annotation.StringRes
import com.psk.mybooks.feature.main.R

/** 하단 탭 순서. Pager 의 페이지 순서와 같다. */
internal enum class MainTab(@StringRes val labelRes: Int) {
    Search(R.string.feature_main_tab_search),
    Favorite(R.string.feature_main_tab_favorite)
}

internal data class MainUiState(val selectedTab: MainTab = MainTab.Search)

internal sealed interface MainIntent {
    /** 탭을 누르거나 Pager 를 넘겨서 탭이 바뀜 */
    data class SelectTab(val tab: MainTab) : MainIntent
}
