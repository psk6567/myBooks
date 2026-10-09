package com.psk.mybooks.feature.favorite.ui

import androidx.lifecycle.viewModelScope
import com.psk.mybooks.core.common.MviViewModel
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.dummyBooks
import kotlinx.coroutines.launch

/** Effect 가 없어서 `Nothing` */
internal class FavoriteViewModel : MviViewModel<FavoriteUiState, FavoriteIntent, Nothing>(FavoriteUiState()) {

    init {
        loadFavoriteBooks()
    }

    override fun onIntent(intent: FavoriteIntent) {
        when (intent) {
            is FavoriteIntent.RemoveFavorite -> reduce { copy(books = books.filterNot { it.id == intent.bookId }) }
        }
    }

    // TODO: 즐겨찾기 UseCase(Room)를 연결한다. 지금은 앞의 몇 권을 즐겨찾기로 가정한다.
    // 검색 화면의 하트와는 아직 연결되어 있지 않다. (둘이 같은 저장소를 보게 되면 함께 반영된다)
    private fun loadFavoriteBooks() {
        viewModelScope.launch {
            val books: List<Book> = dummyBooks().take(DUMMY_FAVORITE_COUNT)
            reduce { copy(books = books) }
        }
    }
}

private const val DUMMY_FAVORITE_COUNT: Int = 6
