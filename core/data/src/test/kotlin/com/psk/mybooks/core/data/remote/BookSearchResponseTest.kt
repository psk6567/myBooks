package com.psk.mybooks.core.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BookSearchResponseTest {

    private val json: Json = Json { ignoreUnknownKeys = true }

    @Test
    fun `카카오 응답을 파싱하고 모르는 필드는 무시한다`() {
        val body: String = """
            {
              "meta": {"total_count": 43, "pageable_count": 43, "is_end": false},
              "documents": [{
                "title": "클린 코드",
                "contents": "소개",
                "url": "https://search.daum.net/x",
                "isbn": "8966260950 9788966260959",
                "datetime": "2013-12-24T00:00:00.000+09:00",
                "authors": ["로버트 C. 마틴"],
                "publisher": "인사이트",
                "translators": ["박재호"],
                "unknown_field": 1,
                "price": 33000,
                "sale_price": 29700,
                "thumbnail": "https://example.com/a.jpg",
                "status": "정상판매"
              }]
            }
        """.trimIndent()

        val response: BookSearchResponse = json.decodeFromString(BookSearchResponse.serializer(), body)

        assertEquals(43, response.meta.totalCount)
        assertTrue(!response.meta.isEnd)
        assertEquals("클린 코드", response.documents.single().title)
        assertEquals(listOf("로버트 C. 마틴"), response.documents.single().authors)
        assertEquals(33000, response.documents.single().price)
        assertEquals(29700, response.documents.single().salePrice)
        assertEquals(listOf("박재호"), response.documents.single().translators)
        assertEquals("2013-12-24T00:00:00.000+09:00", response.documents.single().datetime)
        assertEquals("소개", response.documents.single().contents)
        assertEquals("정상판매", response.documents.single().status)
    }
}
