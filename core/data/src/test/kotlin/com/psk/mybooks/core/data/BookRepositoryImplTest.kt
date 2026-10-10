package com.psk.mybooks.core.data

import androidx.paging.testing.asSnapshot
import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.BookSearchResponse
import com.psk.mybooks.core.data.remote.SearchSort
import com.psk.mybooks.core.data.remote.SearchTarget
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryImplTest {

    private class FakeBookApi : BookApi {
        val calls: MutableList<Triple<Int, Int, SearchSort>> = mutableListOf()

        override suspend fun searchBooks(
            query: String,
            sort: SearchSort,
            page: Int,
            size: Int,
            target: SearchTarget?
        ): BookSearchResponse {
            calls += Triple(page, size, sort)
            val first: Int = (page - 1) * size
            val docs: List<BookSearchResponse.BookDocument> = (first until first + size).map { document(it) }
            return BookSearchResponse(
                BookSearchResponse.Meta(totalCount = 1000, pageableCount = 1000, isEnd = false),
                docs
            )
        }

        private fun document(id: Int): BookSearchResponse.BookDocument = BookSearchResponse.BookDocument(
            title = "책 $id",
            contents = "",
            url = "https://search.daum.net/search?w=bookpage&bookId=$id",
            isbn = "",
            datetime = "",
            authors = emptyList(),
            publisher = "",
            translators = emptyList(),
            price = 1000,
            salePrice = -1,
            thumbnail = "",
            status = ""
        )
    }

    @Test
    fun `첫 로드는 1페이지를 한 페이지 크기로 불러온다`() = runTest {
        val api: FakeBookApi = FakeBookApi()

        val books: List<Book> = BookRepositoryImpl(api)
            .searchBooks("코틀린")
            .asSnapshot()

        assertEquals(
            1,
            api.calls
                .first()
                .first
        )
        assertEquals(
            SEARCH_PAGE_SIZE,
            api.calls
                .first()
                .second
        )
        // 목록 끝에 가까우면 다음 페이지를 미리 불러오므로 한 페이지 이상 받는다
        assertTrue(books.size >= SEARCH_PAGE_SIZE)
    }

    @Test
    fun `끝까지 내리면 다음 페이지를 이어서 불러온다`() = runTest {
        val api: FakeBookApi = FakeBookApi()

        val books: List<Book> = BookRepositoryImpl(api).searchBooks("코틀린").asSnapshot {
            scrollTo(index = SEARCH_PAGE_SIZE)
        }

        assertEquals(
            listOf(1, 2),
            api.calls
                .map { it.first }
                .distinct()
                .take(2)
        )
        assertEquals(
            books
                .map { it.id }
                .distinct(),
            books.map { it.id }
        )
    }

    @Test
    fun `도메인 정렬을 API 정렬로 바꿔서 보낸다`() = runTest {
        val api: FakeBookApi = FakeBookApi()

        BookRepositoryImpl(api)
            .searchBooks("코틀린", SearchSortType.Latest)
            .asSnapshot()
        assertEquals(
            SearchSort.Latest,
            api.calls
                .last()
                .third
        )

        BookRepositoryImpl(api)
            .searchBooks("코틀린", SearchSortType.Accuracy)
            .asSnapshot()
        assertEquals(
            SearchSort.Accuracy,
            api.calls
                .last()
                .third
        )
    }
}
