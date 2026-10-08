package com.psk.mybooks.core.model

/**
 * 도서. 검색·즐겨찾기·상세가 함께 쓴다.
 *
 * @param id 도서를 구분하는 값 (ISBN)
 * @param status 판매 상태 (예: 정상판매, 품절)
 * @param price 정가(원)
 * @param thumbnailUrl 표지 이미지 URL. 없으면 빈 문자열
 */
data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val publisher: String,
    val price: Int,
    val status: String,
    val thumbnailUrl: String
)
