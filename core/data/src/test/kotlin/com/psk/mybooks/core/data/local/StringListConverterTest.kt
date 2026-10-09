package com.psk.mybooks.core.data.local

import org.junit.Assert.assertEquals
import org.junit.Test

class StringListConverterTest {

    private val converter: StringListConverter = StringListConverter()

    private fun roundTrip(list: List<String>): List<String> = converter.toList(converter.fromList(list))

    @Test
    fun `쉼표와 따옴표가 있는 이름도 그대로 돌아온다`() {
        val authors: List<String> = listOf("롤링, J.K.", "\"별명\" 저자", "저자3")

        assertEquals(authors, roundTrip(authors))
    }

    @Test
    fun `빈 목록과 빈 문자열 하나는 구분된다`() {
        assertEquals(emptyList<String>(), roundTrip(emptyList()))
        assertEquals(listOf(""), roundTrip(listOf("")))
    }
}
