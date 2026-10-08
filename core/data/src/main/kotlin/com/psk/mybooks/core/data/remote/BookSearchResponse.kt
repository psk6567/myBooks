package com.psk.mybooks.core.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 카카오 도서 검색 응답 */
@Serializable
internal data class BookSearchResponse(val meta: Meta, val documents: List<BookDocument>) {
    @Serializable
    data class Meta(
        @SerialName("total_count") val totalCount: Int,
        /** 검색된 문서 중 노출 가능한 문서 수 */
        @SerialName("pageable_count") val pageableCount: Int,
        /** 마지막 페이지인지 */
        @SerialName("is_end") val isEnd: Boolean
    )

    /** 카카오가 주는 도서 문서. 도메인 모델은 [toBook] 으로 바꾼다. */
    @Serializable
    data class BookDocument(
        val title: String,
        val contents: String,
        val url: String,
        /** "ISBN10 ISBN13" 형태 (공백으로 구분, 한쪽만 있을 수 있다) */
        val isbn: String,
        /** ISO 8601 (예: 2013-12-24T00:00:00.000+09:00). 비어 있을 수 있다. */
        val datetime: String,
        val authors: List<String>,
        val publisher: String,
        val translators: List<String>,
        /** 정가 */
        val price: Int,
        @SerialName("sale_price") val salePrice: Int,
        /** 표지 URL. 없으면 빈 문자열 */
        val thumbnail: String,
        /** 판매 상태 (정상판매, 품절, 절판 등). 비어 있을 수 있다. */
        val status: String
    )
}
