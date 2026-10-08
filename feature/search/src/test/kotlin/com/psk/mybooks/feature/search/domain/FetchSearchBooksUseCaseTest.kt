package com.psk.mybooks.feature.search.domain

import androidx.paging.PagingData
import com.psk.mybooks.core.domain.BookRepository
import com.psk.mybooks.core.domain.model.Book
import com.psk.mybooks.core.domain.model.SearchSortType
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FetchSearchBooksUseCaseTest {

    private class FakeBookRepository : BookRepository {
        var lastQuery: String? = null
        var lastSort: SearchSortType? = null
        var collectedOn: Thread? = null
        val data: PagingData<Book> = PagingData.empty()

        override fun searchBooks(query: String, sort: SearchSortType): Flow<PagingData<Book>> {
            lastQuery = query
            lastSort = sort
            return flow {
                collectedOn = Thread.currentThread()
                emit(data)
            }
        }
    }

    @Test
    fun `검색어와 정렬을 Repository 에 그대로 전달하고 결과를 돌려준다`() = runTest {
        val repository: FakeBookRepository = FakeBookRepository()
        val dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)

        val result: PagingData<Book> = FetchSearchBooksUseCase(
            repository,
            dispatcher
        )("한강", SearchSortType.Latest).first()

        assertEquals("한강", repository.lastQuery)
        assertEquals(SearchSortType.Latest, repository.lastSort)
        assertSame(repository.data, result)
    }

    @Test
    fun `주입받은 Dispatcher 에서 불러온다`() = runTest {
        val repository: FakeBookRepository = FakeBookRepository()
        val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "io-test") }
        val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

        FetchSearchBooksUseCase(repository, dispatcher)("한강", SearchSortType.Accuracy).first()
        executor.shutdown()

        assertEquals("io-test", repository.collectedOn?.name)
        assertNotEquals(Thread.currentThread(), repository.collectedOn)
    }

    @Test
    fun `검색어가 비어 있으면 Repository 를 부르지 않고 빈 목록을 준다`() = runTest {
        val repository: FakeBookRepository = FakeBookRepository()
        val dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)

        FetchSearchBooksUseCase(repository, dispatcher)("   ", SearchSortType.Accuracy).first()

        assertEquals(null, repository.lastQuery)
    }
}
