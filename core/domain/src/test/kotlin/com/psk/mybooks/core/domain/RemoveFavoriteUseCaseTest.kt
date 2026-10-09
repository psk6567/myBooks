package com.psk.mybooks.core.domain

import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoveFavoriteUseCaseTest {

    private class FakeFavoriteRepository : FavoriteRepository {
        val removed: MutableList<String> = mutableListOf()

        override fun observeFavorites(sort: FavoriteSortType): Flow<List<Book>> = emptyFlow()

        override fun observeFavoriteIds(): Flow<Set<String>> = emptyFlow()

        override suspend fun add(book: Book) = Unit

        override suspend fun remove(bookId: String) {
            removed += bookId
        }
    }

    @Test
    fun `받은 id 의 도서를 즐겨찾기에서 뺀다`() = runTest {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()

        RemoveFavoriteUseCase(repository)("1")

        assertEquals(listOf("1"), repository.removed)
    }
}
