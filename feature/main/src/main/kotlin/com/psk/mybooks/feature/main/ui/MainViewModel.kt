package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.common.MviViewModel

/** Effect 가 없어서 `Nothing` */
internal class MainViewModel : MviViewModel<MainUiState, MainIntent, Nothing>(MainUiState()) {

    override fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectTab -> reduce { copy(selectedTab = intent.tab) }
            is MainIntent.ChangeSearchText -> reduce { copy(searchText = intent.text) }
        }
    }
}
