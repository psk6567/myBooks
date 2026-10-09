package com.psk.mybooks.feature.search.ui

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.domain.AddFavoriteUseCase
import com.psk.mybooks.core.domain.FetchSearchBooksUseCase
import com.psk.mybooks.core.domain.ObserveFavoriteIdsUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.ConcurrentHashMap
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
     * 화면에 내보낸 도서의 원본. 하트를 눌러 저장하려면 도서 전체가 필요한데 화면의 [BookUiModel] 은 표시용 값만 가지므로,
     * 변환하면서 기억해 두었다가 id 로 찾는다. 검색이 바뀌면 비운다. (불러오는 스레드에서 쓰이므로 동시 접근에 안전하게 둔다)
     */
    private val loadedBooks: MutableMap<String, Book> = ConcurrentHashMap()

    /**
     * 검색 결과. 검색어나 정렬이 바뀌면 처음부터 다시 불러오고, 화면 회전에도 목록이 유지된다. (cachedIn)
     * 검색어는 입력이 멈춘 뒤에 반영하고(앞뒤 공백은 무시), 정렬은 바로 반영한다.
     */
    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val books: Flow<PagingData<BookUiModel>> = combine(
        state.map { it.query.trim() }
            .distinctUntilChanged()
            // 비우는 건 바로 반영해서 지운 즉시 결과가 사라지게 한다
            .debounce { query -> if (query.isEmpty()) 0L else QUERY_DEBOUNCE_MILLIS },
        state.map { it.sort }.distinctUntilChanged()
    ) { query: String, sort: SearchSortType -> query to sort }
        .distinctUntilChanged()
        .flatMapLatest { (query, sort) ->
            // 새 검색이면 이전 결과의 원본은 필요 없다
            loadedBooks.clear()
            fetchSearchBooks(query, sort)
        }
        .map { paging ->
            paging.map { book ->
                loadedBooks[book.id] = book
                book.toUiModel()
            }
        }
        .cachedIn(viewModelScope)

    init {
        observeFavorites()
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.ChangeCondition -> reduce { copy(query = intent.query, sort = intent.sort) }
            is SearchIntent.ReportLoadError -> sendError(intent.error)
            is SearchIntent.ToggleFavorite -> toggleFavorite(intent.bookId)
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
    private fun toggleFavorite(bookId: String) {
        viewModelScope.launch {
            try {
                if (bookId in state.value.favoriteIds) {
                    removeFavorite(bookId)
                } else {
                    // 화면에 내보낸 도서만 누를 수 있으므로 원본이 없는 경우는 없다
                    loadedBooks[bookId]?.let { addFavorite(it) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendError(e)
            }
        }
    }
}
