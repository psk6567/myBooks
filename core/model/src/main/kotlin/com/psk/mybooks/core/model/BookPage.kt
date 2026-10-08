package com.psk.mybooks.core.model

/**
 * 도서 검색 결과의 한 페이지.
 *
 * @param books 이 페이지의 도서. [Book.id] 가 서로 겹치지 않는다.
 * @param isEnd 마지막 페이지인지. true 면 다음 페이지를 요청하지 않는다.
 */
data class BookPage(val books: List<Book>, val isEnd: Boolean)
