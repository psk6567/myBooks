package com.psk.mybooks.core.ui

import com.psk.mybooks.core.model.Book
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
        assertEquals(18000, book(price = 20000, salePrice = 18000).toUiModel().salePrice)
        assertNull(book(price = 20000, salePrice = 20000).toUiModel().salePrice)
    }

    @Test
    fun `판매가를 모르면(-1) 판매가가 없다`() {
        assertNull(book(price = 20000, salePrice = -1).toUiModel().salePrice)
    }

    @Test
    fun `저자가 없으면 빈 글자다`() {
        assertEquals("", book(authors = emptyList()).toUiModel().authors)
    }
}
