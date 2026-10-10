package com.psk.mybooks.core.ui

import com.psk.mybooks.core.model.Book
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookUiModelTest {

    private fun book(price: Int = 20000, salePrice: Int = 18000, authors: List<String> = listOf("저자")): Book = Book(
        id = "7",
        title = "제목",
        contents = "소개",
        url = "https://search.daum.net/search?bookId=7",
        isbn = "isbn",
        publishedAt = "2024-01-01T00:00:00.000+09:00",
        authors = authors,
        publisher = "출판사",
        translators = listOf("번역자"),
        price = price,
        salePrice = salePrice,
        thumbnailUrl = "https://example.com/7.jpg",
        status = "정상판매"
    )

    @Test
    fun `표시에 필요한 값만 옮기고 저자는 쉼표로 이어 붙인다`() {
        val ui: BookUiModel = book(authors = listOf("롤링", "클린 코드 저자")).toUiModel()

        assertEquals("7", ui.id)
        assertEquals("제목", ui.title)
        assertEquals("출판사", ui.publisher)
        assertEquals("정상판매", ui.status)
        assertEquals("롤링, 클린 코드 저자", ui.authors)
        assertEquals("https://example.com/7.jpg", ui.thumbnailUrl)
        assertEquals(20000, ui.price)
    }

    @Test
    fun `정가와 판매가가 다를 때만 판매가를 가진다`() {
        assertEquals(
            18000,
            book(price = 20000, salePrice = 18000)
                .toUiModel()
                .salePrice
        )
        assertNull(
            book(price = 20000, salePrice = 20000)
                .toUiModel()
                .salePrice
        )
    }

    @Test
    fun `판매가를 모르면(-1) 판매가가 없다`() {
        assertNull(
            book(price = 20000, salePrice = -1)
                .toUiModel()
                .salePrice
        )
    }

    @Test
    fun `상세에 필요한 값도 옮기고 번역자는 쉼표로 이어 붙이며 출판일은 날짜만 남긴다`() {
        val ui: BookUiModel = book()
            .copy(translators = listOf("번역자1", "번역자2"), isbn = " 9791193478165")
            .toUiModel()

        assertEquals("소개", ui.contents)
        assertEquals("https://search.daum.net/search?bookId=7", ui.url)
        assertEquals("9791193478165", ui.isbn)
        assertEquals("2024-01-01", ui.publishedAt)
        assertEquals("번역자1, 번역자2", ui.translators)
    }

    @Test
    fun `출판일을 모르면 빈 글자고 번역자가 없으면 빈 글자다`() {
        val ui: BookUiModel = book()
            .copy(publishedAt = "", translators = emptyList())
            .toUiModel()

        assertEquals("", ui.publishedAt)
        assertEquals("", ui.translators)
    }

    @Test
    fun `JSON 으로 바꿨다가 되돌리면 같은 값이다(이동 인자로 넘긴다)`() {
        val ui: BookUiModel = book(authors = listOf("롤링", "저자 \"별명\""))
            .copy(translators = listOf("번역자"))
            .toUiModel()

        val restored: BookUiModel = Json.decodeFromString(Json.encodeToString(ui))

        assertEquals(ui, restored)
    }

    @Test
    fun `도메인 모델로 되돌려도 다시 화면용으로 바꾸면 같은 값이다`() {
        val sale: BookUiModel = book(price = 20000, salePrice = 18000, authors = listOf("롤링", "저자 B"))
            .copy(translators = listOf("번역자1", "번역자2"))
            .toUiModel()
        val noSale: BookUiModel = book(price = 20000, salePrice = -1, authors = emptyList()).toUiModel()

        assertEquals(
            sale,
            sale
                .toBook()
                .toUiModel()
        )
        assertEquals(
            noSale,
            noSale
                .toBook()
                .toUiModel()
        )
    }

    @Test
    fun `되돌린 도메인 모델은 저자와 번역자를 다시 나누고 판매가를 모르면 -1 이다`() {
        val restored: Book = book(price = 20000, salePrice = 20000, authors = listOf("롤링", "저자 B"))
            .copy(translators = listOf("번역자"))
            .toUiModel()
            .toBook()

        assertEquals(listOf("롤링", "저자 B"), restored.authors)
        assertEquals(listOf("번역자"), restored.translators)
        assertEquals(-1, restored.salePrice)
        assertEquals(
            emptyList<String>(),
            book(authors = emptyList())
                .toUiModel()
                .toBook()
                .authors
        )
    }

    @Test
    fun `저자가 없으면 빈 글자다`() {
        assertEquals(
            "",
            book(authors = emptyList())
                .toUiModel()
                .authors
        )
    }
}
