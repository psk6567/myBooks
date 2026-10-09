package com.psk.mybooks.core.model

/** 도서 검색 정렬 기준. 서버(카카오 API)가 정렬해 주는 방식만 지원한다. */
enum class SearchSortType {
    /** 정확도순 */
    Accuracy,

    /** 발간일순 (최신 발간일부터) */
    Latest
}
