package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.common.MviViewModel

/** Effect 가 없어서 `Nothing` */
internal class MainViewModel : MviViewModel<MainUiState, MainIntent, Nothing>(MainUiState()) {

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectTab -> reduce { copy(selectedTab = intent.tab) }
            is MainIntent.ChangeSearchText -> reduce { copy(searchText = intent.text) }
            is MainIntent.ChangeSearchSort -> reduce { copy(searchSort = intent.sort) }
            is MainIntent.ChangeFavoriteSort -> reduce { copy(favoriteSort = intent.sort) }
            is MainIntent.ChangeFavoritePriceFilter -> reduce { copy(favoritePriceFilter = intent.priceFilter) }
            is MainIntent.ChangeViewMode -> reduce { copy(viewMode = intent.viewMode) }
            is MainIntent.SelectBook -> reduce { copy(selectedBook = intent.book) }
        }
    }
}
