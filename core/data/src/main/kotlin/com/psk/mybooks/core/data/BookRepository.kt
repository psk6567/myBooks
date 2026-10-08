package com.psk.mybooks.core.data

import com.psk.mybooks.core.model.Book

/** 도서 데이터의 단일 창구. 화면(ViewModel)은 API 나 DB 를 직접 모르고 이것만 쓴다. */
interface BookRepository {

    /**
     * 도서를 검색한다. 실패(네트워크, 서버 오류 등)는 예외를 던지지 않고 [Result.failure] 로 돌려준다.
     *
     * @param query 검색어. 공백뿐이면 서버를 부르지 않고 빈 목록을 돌려준다.
     */
    suspend fun searchBooks(query: String): Result<List<Book>>
}
