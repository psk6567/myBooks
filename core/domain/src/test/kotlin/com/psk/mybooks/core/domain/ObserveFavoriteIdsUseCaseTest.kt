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

class ObserveFavoriteIdsUseCaseTest {

    private class FakeFavoriteRepository : FavoriteRepository {
        var collectedOn: Thread? = null

        override fun observeFavorites(sort: FavoriteSortType, priceFilter: PriceFilter): Flow<List<Book>> = emptyFlow()

        override fun observeFavoriteIds(): Flow<Set<String>> = flow {
            collectedOn = Thread.currentThread()
            emit(setOf("1", "2"))
        }

        override suspend fun add(book: Book) = Unit

        override suspend fun remove(bookId: String) = Unit
    }

    @Test
    fun `Repository 의 id 모음을 그대로 돌려준다`() = runTest {
        val dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)

        val ids: Set<String> = ObserveFavoriteIdsUseCase(FakeFavoriteRepository(), dispatcher)().first()

        assertEquals(setOf("1", "2"), ids)
    }

    @Test
    fun `주입받은 Dispatcher 에서 읽는다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "io-test") }
        val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

        ObserveFavoriteIdsUseCase(repository, dispatcher)().first()
        executor.shutdown()

        assertEquals(
            "io-test",
            repository.collectedOn
                ?.name
        )
        assertNotEquals(Thread.currentThread(), repository.collectedOn)
    }
}
