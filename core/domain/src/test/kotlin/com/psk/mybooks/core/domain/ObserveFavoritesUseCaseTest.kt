package com.psk.mybooks.core.domain

import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ObserveFavoritesUseCaseTest {

    private class FakeFavoriteRepository : FavoriteRepository {
        var lastSort: FavoriteSortType? = null
        var lastFilter: PriceFilter? = null
        var collectedOn: Thread? = null
        val books: List<Book> = emptyList()

        override fun observeFavorites(sort: FavoriteSortType, priceFilter: PriceFilter): Flow<List<Book>> {
            lastSort = sort
            lastFilter = priceFilter
            return flow {
                collectedOn = Thread.currentThread()
                emit(books)
            }
        }

        override fun observeFavoriteIds(): Flow<Set<String>> = emptyFlow()

        override suspend fun add(book: Book) = Unit

        override suspend fun remove(bookId: String) = Unit
    }

    @Test
    fun `정렬 기준과 금액 필터를 Repository 에 그대로 전달하고 결과를 돌려준다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)

        val filter: PriceFilter = PriceFilter(min = 1000, max = 20000)

        val result: List<Book> =
            ObserveFavoritesUseCase(repository, dispatcher)(FavoriteSortType.PriceDesc, filter).first()

        assertEquals(FavoriteSortType.PriceDesc, repository.lastSort)
        assertEquals(filter, repository.lastFilter)
        assertEquals(repository.books, result)
    }

    @Test
    fun `주입받은 Dispatcher 에서 읽는다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "io-test") }
        val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

        ObserveFavoritesUseCase(repository, dispatcher)(FavoriteSortType.TitleAsc).first()
        executor.shutdown()

        assertEquals(
            "io-test",
            repository.collectedOn
                ?.name
        )
        assertNotEquals(Thread.currentThread(), repository.collectedOn)
    }
}
