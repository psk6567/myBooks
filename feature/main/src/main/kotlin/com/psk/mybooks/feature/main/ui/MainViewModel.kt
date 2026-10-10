package com.psk.mybooks.feature.main.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.IsOnlineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 화면 조건(탭, 검색어, 정렬, 필터, 보기 방식, 선택한 도서)을 `SavedStateHandle` 에 계속 저장한다.
 * 프로세스가 종료됐다가 돌아와도 이전 상태로 이어지고, 이때는 앱을 새로 연 것이 아니므로 오프라인 이동도 하지 않는다.
 */
@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    isOnline: IsOnlineUseCase
) : MviViewModel<MainUiState, MainIntent, MainEffect>(
    mainUiStateFromSaved(savedStateHandle[MAIN_SAVED_STATE_KEY]) ?: MainUiState()
) {

    init {
        val restored: Boolean = mainUiStateFromSaved(savedStateHandle[MAIN_SAVED_STATE_KEY]) != null
        // 앱을 처음 열었을 때 인터넷이 없으면 검색은 쓸 수 없으므로 즐겨찾기 탭에서 시작하고 이유를 알려 준다.
        // (즐겨찾기가 없어도 옮긴다. 검색 탭으로 돌아가는 건 사용자가 정한다)
        if (!restored && !isOnline()) {
            reduce { copy(selectedTab = MainTab.Favorite) }
            sendEffect(MainEffect.MovedToFavoriteOffline)
        }
        state
            .onEach { savedStateHandle[MAIN_SAVED_STATE_KEY] = it.toSavedJson() }
            .launchIn(viewModelScope)
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
