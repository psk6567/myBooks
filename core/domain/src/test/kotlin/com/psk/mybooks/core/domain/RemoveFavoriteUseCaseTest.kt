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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RemoveFavoriteUseCaseTest {

    private class FakeFavoriteRepository : FavoriteRepository {
        val removed: MutableList<String> = mutableListOf()
        var removedOn: Thread? = null

        override fun observeFavorites(sort: FavoriteSortType, priceFilter: PriceFilter): Flow<List<Book>> = emptyFlow()

        override fun observeFavoriteIds(): Flow<Set<String>> = emptyFlow()

        override suspend fun add(book: Book) = Unit

        override suspend fun remove(bookId: String) {
            removed += bookId
            removedOn = Thread.currentThread()
        }
    }

    @Test
    fun `받은 id 의 도서를 즐겨찾기에서 뺀다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)

        RemoveFavoriteUseCase(repository, dispatcher)("1")

        assertEquals(listOf("1"), repository.removed)
    }

    @Test
    fun `주입받은 Dispatcher 에서 뺀다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "io-test") }
        val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

        RemoveFavoriteUseCase(repository, dispatcher)("1")
        executor.shutdown()

        assertEquals(
            "io-test",
            repository.removedOn
                ?.name
        )
        assertNotEquals(Thread.currentThread(), repository.removedOn)
    }
}
