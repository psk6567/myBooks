package com.psk.mybooks.core.model

/**
 * 도서. 검색·즐겨찾기·상세가 함께 쓴다. (카카오 도서 검색 API 의 문서 하나에 해당)
 *
 * @param id 도서를 구분하는 값. 한 도서 항목마다 하나이며 즐겨찾기 저장 등의 키로 쓴다.
 * ISBN 은 월간지처럼 호수마다 제목·가격·표지가 달라도 같은 값을 쓰는 경우가 있어서 키로 쓰지 않는다.
 * @param title 도서 제목
 * @param contents 도서 소개. 목록용으로 잘려서 온다.
 * @param url 도서 상세 URL
 * @param isbn ISBN10 과 ISBN13 중 하나 이상. 둘 다 있으면 공백으로 구분한다.
 * @param publishedAt 출판일시. API 가 주는 ISO 8601 문자열 그대로이며(예: `2013-12-24T00:00:00.000+09:00`) 알 수 없으면 빈 문자열.
 * 모든 값의 형식이 같아 문자열 비교가 곧 날짜순이다. 날짜 타입이 필요해지면 그때 변환한다.
 * @param authors 저자 목록
 * @param publisher 출판사
 * @param translators 번역자 목록. 없으면 빈 목록
 * @param price 정가(원)
 * @param salePrice 판매가(원). 알 수 없으면 -1 (API 가 그렇게 준다)
 * @param thumbnailUrl 표지 이미지 URL. 없으면 빈 문자열
 * @param status 판매 상태(정상판매, 품절, 절판 등). 상황에 따라 바뀔 수 있어 값으로 분기하지 않고 화면에 보여 주기만 한다.
 */
data class Book(
    val id: String,
    val title: String,
    val contents: String,
    val url: String,
    val isbn: String,
    val publishedAt: String,
    val authors: List<String>,
    val publisher: String,
    val translators: List<String>,
    val price: Int,
    val salePrice: Int,
    val thumbnailUrl: String,
    val status: String
)
