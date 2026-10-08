package com.psk.mybooks.core.data

import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.BookSearchResponse
import com.psk.mybooks.core.data.remote.SearchSort
import com.psk.mybooks.core.data.remote.SearchTarget
import com.psk.mybooks.core.model.Book
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BookRepositoryImplTest {

    private class FakeBookApi(var result: () -> BookSearchResponse) : BookApi {
        var callCount: Int = 0
        var lastQuery: String? = null

        override suspend fun searchBooks(
            query: String,
            sort: SearchSort,
            page: Int,
            size: Int,
            target: SearchTarget?
        ): BookSearchResponse {
            callCount++
            lastQuery = query
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

    private fun response(vararg docs: BookSearchResponse.BookDocument): BookSearchResponse = BookSearchResponse(
        BookSearchResponse.Meta(totalCount = docs.size, pageableCount = docs.size, isEnd = true),
        docs.toList()
    )

    @Test
    fun `검색 결과를 도메인 모델 목록으로 바꾼다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response(document(1), document(2)) }

        val books: List<Book> = BookRepositoryImpl(api).searchBooks("코틀린").getOrThrow()

        assertEquals(listOf("1", "2"), books.map { it.id })
        assertEquals("코틀린", api.lastQuery)
    }

    @Test
    fun `ISBN 이 같아도 다른 항목이면 모두 남기고, 같은 항목이 두 번 오면 하나만 남긴다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response(document(1), document(2), document(1, title = "다시 온 책 1")) }

        val books: List<Book> = BookRepositoryImpl(api).searchBooks("한강").getOrThrow()

        assertEquals(listOf("1", "2"), books.map { it.id })
        assertEquals("책 1", books.first().title)
    }

    @Test
    fun `검색어 앞뒤 공백은 지우고 보낸다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response() }

        BookRepositoryImpl(api).searchBooks("  코틀린 ")

        assertEquals("코틀린", api.lastQuery)
    }

    @Test
    fun `검색어가 비어 있으면 서버를 부르지 않고 빈 목록을 준다`() = runTest {
        val api: FakeBookApi = FakeBookApi { error("호출되면 안 된다") }

        val books: List<Book> = BookRepositoryImpl(api).searchBooks("   ").getOrThrow()

        assertEquals(0, api.callCount)
        assertTrue(books.isEmpty())
    }

    @Test
    fun `네트워크 오류는 예외가 아니라 실패 결과로 돌려준다`() = runTest {
        val error: IOException = IOException("offline")
        val api: FakeBookApi = FakeBookApi { throw error }

        val result: Result<List<Book>> = BookRepositoryImpl(api).searchBooks("코틀린")

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
}
