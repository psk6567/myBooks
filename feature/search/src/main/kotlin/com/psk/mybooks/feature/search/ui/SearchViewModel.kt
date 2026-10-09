package com.psk.mybooks.feature.search.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.feature.search.domain.FetchSearchBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/** 입력이 멈춘 뒤 이만큼 기다렸다가 검색한다 */
private const val QUERY_DEBOUNCE_MILLIS: Long = 300

/** Effect 가 없어서 `Nothing` */
@HiltViewModel
internal class SearchViewModel @Inject constructor(private val fetchSearchBooks: FetchSearchBooksUseCase) :
    MviViewModel<SearchUiState, SearchIntent, Nothing>(SearchUiState()) {

    /**
     * 검색 결과. 검색어나 정렬이 바뀌면 처음부터 다시 불러오고, 화면 회전에도 목록이 유지된다. (cachedIn)
     * 검색어는 입력이 멈춘 뒤에 반영하고(앞뒤 공백은 무시), 정렬은 바로 반영한다.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val books: Flow<PagingData<Book>> = combine(
        state.map { it.query.trim() }
            .distinctUntilChanged()
            // 비우는 건 바로 반영해서 지운 즉시 결과가 사라지게 한다
            .debounce { query -> if (query.isEmpty()) 0L else QUERY_DEBOUNCE_MILLIS },
        state.map { it.sort }.distinctUntilChanged()
    ) { query: String, sort: SearchSortType -> query to sort }
        .distinctUntilChanged()
        .flatMapLatest { (query, sort) -> fetchSearchBooks(query, sort) }
        .cachedIn(viewModelScope)

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.ChangeCondition -> reduce { copy(query = intent.query, sort = intent.sort) }
            is SearchIntent.ToggleFavorite -> toggleFavorite(intent.bookId)
        }
    }

    private fun toggleFavorite(bookId: String) {
        reduce {
            val ids: Set<String> = if (bookId in favoriteIds) favoriteIds - bookId else favoriteIds + bookId
            copy(favoriteIds = ids)
        }
    }
}
