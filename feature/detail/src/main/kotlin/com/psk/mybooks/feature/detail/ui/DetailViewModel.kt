package com.psk.mybooks.feature.detail.ui

import androidx.lifecycle.viewModelScope
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.AddFavoriteUseCase
import com.psk.mybooks.core.domain.ObserveFavoriteIdsUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.toBook
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * 보여 줄 도서를 직접 받는다. 화면 이동(전체 화면)과 목록 옆 패널(넓은 화면)이 같은 상세를 쓰기 위해서다.
 * 도서마다 ViewModel 이 따로 만들어진다.
 */
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
internal class DetailViewModel @AssistedInject constructor(
    @Assisted book: BookUiModel,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase
) : MviViewModel<DetailUiState, DetailIntent, DetailEffect>(DetailUiState(book = book)) {

    @AssistedFactory
    interface Factory {
        fun create(book: BookUiModel): DetailViewModel
    }

    init {
        observeFavorite()
    }

    override fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.Back -> sendEffect(DetailEffect.Close)
            DetailIntent.ToggleFavorite -> toggleFavorite()
        }
    }

    /** 즐겨찾기 id 를 계속 받아 하트에 반영한다. 다른 화면에서 바꿔도 따라간다. */
    private fun observeFavorite() {
        val book: BookUiModel = state.value.book
        viewModelScope.launch {
            observeFavoriteIds()
                .catch { sendError(it) }
                .collect { ids -> reduce { copy(isFavorite = book.id in ids) } }
        }
    }

    /**
     * 즐겨찾기를 켜거나 끈다. 담을 때는 도서 전체가 필요해서 화면용 값을 도메인 모델로 되돌려 저장한다.
     * 상태는 직접 바꾸지 않고 저장소 흐름이 알려 주는 값으로 바뀐다.
     */
    private fun toggleFavorite() {
        val current: DetailUiState = state.value
        viewModelScope.launch {
            try {
                if (current.isFavorite) {
                    removeFavorite(current.book.id)
                } else {
                    addFavorite(
                        current.book
                            .toBook()
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendError(e)
            }
        }
    }
}
