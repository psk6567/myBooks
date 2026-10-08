package com.psk.mybooks.core.data.remote

import com.psk.mybooks.core.domain.model.Book
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BookMapperTest {

    private fun document(
        isbn: String = "8966260950 9788966260959",
        datetime: String = "2013-12-24T00:00:00.000+09:00",
        url: String = "https://search.daum.net/x"
    ) = BookSearchResponse.BookDocument(
        title = "클린 코드",
        contents = "소개",
        url = url,
        isbn = isbn,
        datetime = datetime,
        authors = listOf("로버트 C. 마틴"),
        publisher = "인사이트",
        translators = listOf("박재호"),
        price = 33000,
        salePrice = 29700,
        thumbnail = "https://example.com/a.jpg",
        status = "정상판매"
    )

    @Test
    fun `모든 필드가 도서 모델로 옮겨진다`() {
        val book: Book = document().toBook()

        assertEquals("클린 코드", book.title)
        assertEquals("소개", book.contents)
        assertEquals("https://search.daum.net/x", book.url)
        assertEquals("https://search.daum.net/x", book.id)
        assertEquals("2013-12-24T00:00:00.000+09:00", book.publishedAt)
        assertEquals(listOf("로버트 C. 마틴"), book.authors)
        assertEquals("인사이트", book.publisher)
        assertEquals(listOf("박재호"), book.translators)
        assertEquals(33000, book.price)
        assertEquals(29700, book.salePrice)
        assertEquals("https://example.com/a.jpg", book.thumbnailUrl)
        assertEquals("정상판매", book.status)
    }

    @Test
    fun `id 는 상세 URL 의 bookId 이다`() {
        val url: String = "https://search.daum.net/search?w=bookpage&bookId=927035&q=Clean+Code"

        assertEquals("927035", document(url = url).toBook().id)
    }

    @Test
    fun `ISBN 이 같아도 URL 이 다르면 id 가 다르다`() {
        val a: Book = document(isbn = "9772383695005", url = "https://x?bookId=5411959").toBook()
        val b: Book = document(isbn = "9772383695005", url = "https://x?bookId=5487266").toBook()

        assertNotEquals(a.id, b.id)
    }

    @Test
    fun `bookId 가 없으면 URL 을, URL 도 없으면 ISBN 을 id 로 쓴다`() {
        assertEquals("https://x/y", document(url = "https://x/y").toBook().id)
        assertEquals("8966260950 9788966260959", document(url = "").toBook().id)
    }

    @Test
    fun `출판일시는 API 가 준 문자열 그대로 둔다`() {
        assertEquals("", document(datetime = "").toBook().publishedAt)
    }
}
