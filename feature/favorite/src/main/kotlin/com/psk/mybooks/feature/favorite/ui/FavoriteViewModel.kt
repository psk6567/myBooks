package com.psk.mybooks.feature.favorite.ui

import androidx.lifecycle.viewModelScope
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.ObserveFavoritesUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.FavoriteSortType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Effect 가 없어서 `Nothing` */
@HiltViewModel
internal class FavoriteViewModel @Inject constructor(
    private val observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase
) : MviViewModel<FavoriteUiState, FavoriteIntent, Nothing>(FavoriteUiState()) {

    init {
        observeBooks()
    }

    override fun onIntent(intent: FavoriteIntent) {
        when (intent) {
            is FavoriteIntent.ChangeSort -> reduce { copy(sort = intent.sort) }
            is FavoriteIntent.RemoveFavorite -> viewModelScope.launch { removeFavorite(intent.bookId) }
        }
    }

    /** 정렬이 바뀌면 그 순서로 다시 받고, 어디서든 담거나 빼면 목록이 바로 바뀐다. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBooks() {
        viewModelScope.launch {
            state.map { it.sort }
                .distinctUntilChanged()
                .flatMapLatest { sort: FavoriteSortType -> observeFavorites(sort) }
                .collect { books -> reduce { copy(books = books) } }
        }
    }
}
