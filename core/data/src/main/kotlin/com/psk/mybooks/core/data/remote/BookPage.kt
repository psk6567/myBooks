package com.psk.mybooks.core.data.remote

import com.psk.mybooks.core.domain.model.Book

/** 검색 응답 한 번을 도메인 모델로 바꾼 결과. 화면에는 [BookPagingSource] 를 거친 목록만 나간다. */
internal data class BookPage(val books: List<Book>, val isEnd: Boolean)
