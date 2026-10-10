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

class AddFavoriteUseCaseTest {

    private class FakeFavoriteRepository : FavoriteRepository {
        val added: MutableList<Book> = mutableListOf()
        var addedOn: Thread? = null

        override fun observeFavorites(
            sort: FavoriteSortType,
            priceFilter: PriceFilter,
            query: String
        ): Flow<List<Book>> = emptyFlow()

        override fun observeFavoriteIds(): Flow<Set<String>> = emptyFlow()

        override suspend fun add(book: Book) {
            added += book
            addedOn = Thread.currentThread()
        }

        override suspend fun remove(bookId: String) = Unit
    }

    private fun book(): Book = Book(
        id = "1",
        title = "제목",
        contents = "",
        url = "",
        isbn = "",
        publishedAt = "",
        authors = emptyList(),
        publisher = "",
        translators = emptyList(),
        price = 0,
        salePrice = -1,
        thumbnailUrl = "",
        status = ""
    )

    @Test
    fun `받은 도서를 즐겨찾기에 담는다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val dispatcher: CoroutineDispatcher = StandardTestDispatcher(testScheduler)
        val book: Book = book()

        AddFavoriteUseCase(repository, dispatcher)(book)

        assertEquals(listOf(book), repository.added)
    }

    @Test
    fun `주입받은 Dispatcher 에서 저장한다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val executor: ExecutorService = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "io-test") }
        val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

        AddFavoriteUseCase(repository, dispatcher)(book())
        executor.shutdown()

        assertEquals(
            "io-test",
            repository.addedOn
                ?.name
        )
        assertNotEquals(Thread.currentThread(), repository.addedOn)
    }
}
