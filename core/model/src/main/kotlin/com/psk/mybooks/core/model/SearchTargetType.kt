package com.psk.mybooks.core.model

/** 도서 검색에서 검색어를 찾을 필드. 서버(카카오 API)의 검색 필드 제한(`target`)에 해당한다. */
enum class SearchTargetType {
    /** 제한 없음. 서버가 제목, 저자, 출판사 등 전체에서 찾는다. */
    All,

    /** 제목 */
    Title,

    /** ISBN */
    Isbn,

    /** 출판사 */
    Publisher,

    /** 저자 (API 에서는 인명(`person`)이다) */
    Author
}
