package com.psk.mybooks.feature.search.domain

import androidx.paging.PagingData
import com.psk.mybooks.core.data.BookRepository
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import kotlinx.coroutines.flow.Flow

/**
 * 검색어와 정렬로 도서를 검색한다. ViewModel 은 Repository 를 직접 알지 않고 이 UseCase 만 쓴다.
 * 지금은 Repository 를 그대로 전달하지만, 검색 결과에 즐겨찾기 여부를 합치는 등의 규칙은 여기에 둔다.
 */
internal class FetchSearchBooksUseCase(private val repository: BookRepository) {

    operator fun invoke(query: String, sort: SearchSortType): Flow<PagingData<Book>> =
        repository.searchBooks(query = query, sort = sort)
}
