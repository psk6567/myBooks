package com.psk.mybooks.core.data

import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.BookSearchResponse
import com.psk.mybooks.core.data.remote.SearchSort
import com.psk.mybooks.core.data.remote.SearchTarget
import com.psk.mybooks.core.model.BookPage
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryImplTest {

    private class FakeBookApi(var result: () -> BookSearchResponse) : BookApi {
        var callCount: Int = 0
        var lastQuery: String? = null
        var lastPage: Int? = null
        var lastSize: Int? = null

        override suspend fun searchBooks(
            query: String,
            sort: SearchSort,
            page: Int,
            size: Int,
            target: SearchTarget?
        ): BookSearchResponse {
            callCount++
            lastQuery = query
            lastPage = page
            lastSize = size
            return result()
        }
    }

    private fun document(bookId: Int, title: String = "책 $bookId"): BookSearchResponse.BookDocument =
        BookSearchResponse.BookDocument(
            title = title,
            contents = "",
            url = "https://search.daum.net/search?w=bookpage&bookId=$bookId",
            isbn = "9772383695005",
            datetime = "2022-02-28T00:00:00.000+09:00",
            authors = emptyList(),
            publisher = "",
            translators = emptyList(),
            price = 1000,
            salePrice = -1,
            thumbnail = "",
            status = ""
        )

    private fun response(vararg docs: BookSearchResponse.BookDocument, isEnd: Boolean = false): BookSearchResponse =
        BookSearchResponse(
            BookSearchResponse.Meta(totalCount = docs.size, pageableCount = docs.size, isEnd = isEnd),
            docs.toList()
        )

    @Test
    fun `검색 결과를 도메인 페이지로 바꾼다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response(document(1), document(2), isEnd = true) }

        val page: BookPage = BookRepositoryImpl(api).searchBooks("코틀린", page = 2, size = 20).getOrThrow()

        assertEquals(listOf("1", "2"), page.books.map { it.id })
        assertTrue(page.isEnd)
        assertEquals("코틀린", api.lastQuery)
        assertEquals(2, api.lastPage)
        assertEquals(20, api.lastSize)
    }

    @Test
    fun `ISBN 이 같아도 다른 항목이면 모두 남기고, 같은 항목이 두 번 오면 하나만 남긴다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response(document(1), document(2), document(1, title = "다시 온 책 1")) }

        val page: BookPage = BookRepositoryImpl(api).searchBooks("한강").getOrThrow()

        assertEquals(listOf("1", "2"), page.books.map { it.id })
        assertEquals("책 1", page.books.first().title)
    }

    @Test
    fun `검색어 앞뒤 공백은 지우고 보낸다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response() }

        BookRepositoryImpl(api).searchBooks("  코틀린 ")

        assertEquals("코틀린", api.lastQuery)
    }

    @Test
    fun `검색어가 비어 있으면 서버를 부르지 않고 빈 마지막 페이지를 준다`() = runTest {
        val api: FakeBookApi = FakeBookApi { error("호출되면 안 된다") }

        val page: BookPage = BookRepositoryImpl(api).searchBooks("   ").getOrThrow()

        assertEquals(0, api.callCount)
        assertTrue(page.books.isEmpty())
        assertTrue(page.isEnd)
    }

    @Test
    fun `네트워크 오류는 예외가 아니라 실패 결과로 돌려준다`() = runTest {
        val error: IOException = IOException("offline")
        val api: FakeBookApi = FakeBookApi { throw error }

        val result: Result<BookPage> = BookRepositoryImpl(api).searchBooks("코틀린")

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `코루틴 취소는 실패 결과로 삼키지 않고 다시 던진다`() = runTest {
        val api: FakeBookApi = FakeBookApi { throw CancellationException("cancelled") }

        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines.runBlocking { BookRepositoryImpl(api).searchBooks("코틀린") }
        }
    }

    @Test
    fun `페이지나 크기가 범위를 벗어나면 호출하는 쪽의 실수로 보고 예외를 던진다`() = runTest {
        val repository: BookRepositoryImpl = BookRepositoryImpl(FakeBookApi { response() })

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { repository.searchBooks("a", page = 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { repository.searchBooks("a", page = 51) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking { repository.searchBooks("a", size = 51) }
        }
        assertFalse(repository.searchBooks("a", page = 50, size = 50).isFailure)
    }
}
