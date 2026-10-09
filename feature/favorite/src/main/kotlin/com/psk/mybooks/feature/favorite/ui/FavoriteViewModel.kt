package com.psk.mybooks.feature.favorite.ui

import androidx.lifecycle.viewModelScope
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.ObserveFavoritesUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.ui.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
internal class FavoriteViewModel @Inject constructor(
    private val observeFavorites: ObserveFavoritesUseCase,
    private val removeFavorite: RemoveFavoriteUseCase
) : MviViewModel<FavoriteUiState, FavoriteIntent, FavoriteEffect>(FavoriteUiState()) {

    init {
        observeBooks()
    }

    override fun onIntent(intent: FavoriteIntent) {
        when (intent) {
            is FavoriteIntent.ChangeCondition -> reduce { copy(sort = intent.sort, priceFilter = intent.priceFilter) }
            is FavoriteIntent.OpenDetail -> sendEffect(FavoriteEffect.OpenDetail(intent.book))
            is FavoriteIntent.RemoveFavorite -> remove(intent.bookId)
        }
    }

    /** 정렬이나 필터가 바뀌면 그 조건으로 다시 받고, 어디서든 담거나 빼면 목록이 바로 바뀐다. */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeBooks() {
        viewModelScope.launch {
            state.map { it.sort to it.priceFilter }
                .distinctUntilChanged()
                .flatMapLatest { (sort, priceFilter) -> observeFavorites(sort, priceFilter) }
                .catch { sendError(it) }
                .collect { books -> reduce { copy(books = books.map { it.toUiModel() }) } }
        }
    }

    private fun remove(bookId: String) {
        viewModelScope.launch {
            try {
                removeFavorite(bookId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendError(e)
            }
        }
    }
}
