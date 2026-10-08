package com.psk.mybooks.feature.search.ui

import androidx.lifecycle.viewModelScope
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.data.dummyBooks
import com.psk.mybooks.core.model.Book
import kotlinx.coroutines.launch

/** Effect 가 없어서 `Nothing` */
internal class SearchViewModel : MviViewModel<SearchUiState, SearchIntent, Nothing>(SearchUiState()) {

    init {
        loadSearchBooks()
    }

    override fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.ToggleFavorite -> toggleFavorite(intent.bookId)
        }
    }

    // TODO: 검색 UseCase 를 연결한다. 검색어·정렬·페이지가 생기면 파라미터로 받는다.
    private fun loadSearchBooks() {
        viewModelScope.launch {
            val books: List<Book> = dummyBooks()
            reduce { copy(books = books) }
        }
    }

    private fun toggleFavorite(bookId: String) {
        reduce {
            val ids: Set<String> = if (bookId in favoriteIds) favoriteIds - bookId else favoriteIds + bookId
            copy(favoriteIds = ids)
        }
    }
}
