package com.psk.mybooks.core.domain

import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AddFavoriteUseCaseTest {

    private class FakeFavoriteRepository : FavoriteRepository {
        val added: MutableList<Book> = mutableListOf()

        override fun observeFavorites(sort: FavoriteSortType): Flow<List<Book>> = emptyFlow()

        override fun observeFavoriteIds(): Flow<Set<String>> = emptyFlow()

        override suspend fun add(book: Book) {
            added += book
        }

        override suspend fun remove(bookId: String) = Unit
    }

    @Test
    fun `받은 도서를 즐겨찾기에 담는다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val book: Book = Book(
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

        AddFavoriteUseCase(repository)(book)

        assertEquals(listOf(book), repository.added)
    }
}
