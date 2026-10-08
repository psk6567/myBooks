package com.psk.mybooks.feature.search.domain

import androidx.paging.PagingData
import com.psk.mybooks.core.domain.BookRepository
import com.psk.mybooks.core.domain.model.Book
import com.psk.mybooks.core.domain.model.SearchSortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FetchSearchBooksUseCaseTest {

    private class FakeBookRepository : BookRepository {
        var lastQuery: String? = null
        var lastSort: SearchSortType? = null
        val result: Flow<PagingData<Book>> = flowOf(PagingData.empty())

        override fun searchBooks(query: String, sort: SearchSortType): Flow<PagingData<Book>> {
            lastQuery = query
            lastSort = sort
            return result
        }
    }

    @Test
    fun `검색어와 정렬을 Repository 에 그대로 전달하고 결과를 돌려준다`() {
        val repository: FakeBookRepository = FakeBookRepository()

        val result: Flow<PagingData<Book>> = FetchSearchBooksUseCase(repository)("한강", SearchSortType.Latest)

        assertEquals("한강", repository.lastQuery)
        assertEquals(SearchSortType.Latest, repository.lastSort)
        assertSame(repository.result, result)
    }
}
