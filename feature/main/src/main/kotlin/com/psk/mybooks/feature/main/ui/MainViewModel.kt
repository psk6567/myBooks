package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.IsOnlineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    isOnline: IsOnlineUseCase
) : MviViewModel<MainUiState, MainIntent, MainEffect>(MainUiState()) {

    init {
        // 앱을 열었을 때 인터넷이 없으면 검색은 쓸 수 없으므로 즐겨찾기 탭에서 시작하고 이유를 알려 준다.
        // (즐겨찾기가 없어도 옮긴다. 검색 탭으로 돌아가는 건 사용자가 정한다)
        if (!isOnline()) {
            reduce { copy(selectedTab = MainTab.Favorite) }
            sendEffect(MainEffect.MovedToFavoriteOffline)
        }
    }

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
