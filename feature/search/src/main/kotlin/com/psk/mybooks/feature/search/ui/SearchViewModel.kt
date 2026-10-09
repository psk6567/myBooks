package com.psk.mybooks.feature.search.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.AddFavoriteUseCase
import com.psk.mybooks.core.domain.FetchSearchBooksUseCase
import com.psk.mybooks.core.domain.ObserveFavoriteIdsUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** 입력이 멈춘 뒤 이만큼 기다렸다가 검색한다 */
private const val QUERY_DEBOUNCE_MILLIS: Long = 300

/** Effect 가 없어서 `Nothing` */
@HiltViewModel
internal class SearchViewModel @Inject constructor(
    private val fetchSearchBooks: FetchSearchBooksUseCase,
    private val observeFavoriteIds: ObserveFavoriteIdsUseCase,
    private val addFavorite: AddFavoriteUseCase,
    private val removeFavorite: RemoveFavoriteUseCase
) : MviViewModel<SearchUiState, SearchIntent, Nothing>(SearchUiState()) {

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

    /** 마지막으로 알린 불러오기 오류. 같은 오류 객체를 다시 알리지 않는다(화면을 회전해도 로드 상태가 같은 오류를 다시 준다). */
    private var lastReportedLoadError: Throwable? = null

    init {
        observeFavorites()
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.ChangeCondition -> reduce { copy(query = intent.query, sort = intent.sort) }
            is SearchIntent.ReportLoadError -> reportLoadError(intent.error)
            is SearchIntent.ToggleFavorite -> toggleFavorite(intent.book)
        }
    }

    /** 즐겨찾기한 id 를 계속 받아 하트에 반영한다. 즐겨찾기 탭에서 빼도 여기 하트가 꺼진다. */
    private fun observeFavorites() {
        viewModelScope.launch {
            observeFavoriteIds()
                .catch { sendError(it) }
                .collect { ids -> reduce { copy(favoriteIds = ids) } }
        }
    }

    /** 저장소를 바꾸면 위 흐름이 새 id 를 주므로 상태는 직접 바꾸지 않는다. */
    private fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            try {
                if (book.id in state.value.favoriteIds) removeFavorite(book.id) else addFavorite(book)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendError(e)
            }
        }
    }

    private fun reportLoadError(error: Throwable) {
        if (error === lastReportedLoadError) return
        lastReportedLoadError = error
        sendError(error)
    }
}
