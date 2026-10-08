package com.psk.mybooks.feature.search.domain

import androidx.paging.PagingData
import com.psk.mybooks.core.common.IoDispatcher
import com.psk.mybooks.core.domain.BookRepository
import com.psk.mybooks.core.domain.model.Book
import com.psk.mybooks.core.domain.model.SearchSortType
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * 검색어와 정렬로 도서를 검색한다. ViewModel 은 Repository 를 직접 알지 않고 이 UseCase 만 쓴다.
 *
 * 불러오는 작업은 [ioDispatcher] 에서 실행한다. 그래서 viewModelScope(Main)에서 불러도 스레드를 신경 쓸 필요가 없다.
 * 지금은 Repository 를 그대로 전달하지만, 검색 결과에 즐겨찾기 여부를 합치는 등의 규칙은 여기에 둔다.
 */
internal class FetchSearchBooksUseCase @Inject constructor(
    private val repository: BookRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke(query: String, sort: SearchSortType): Flow<PagingData<Book>> =
        repository.searchBooks(query = query, sort = sort).flowOn(ioDispatcher)
}
