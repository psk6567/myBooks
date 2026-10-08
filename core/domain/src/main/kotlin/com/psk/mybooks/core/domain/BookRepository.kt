package com.psk.mybooks.core.domain

import androidx.paging.PagingData
import com.psk.mybooks.core.domain.model.Book
import com.psk.mybooks.core.domain.model.SearchSortType
import kotlinx.coroutines.flow.Flow

/** 도서 데이터의 단일 창구. 화면(ViewModel)은 API 나 DB 를 직접 모르고 이것만 쓴다. */
interface BookRepository {

    /**
     * 도서를 검색해 페이지 단위로 흘려보낸다. 다음 페이지는 목록 끝에 가까워지면 자동으로 불러온다.
     * 검색어나 정렬이 바뀌면 새로 호출해 처음부터 다시 불러온다.
     *
     * 불러오기 실패는 예외가 아니라 화면이 읽는 `loadState` 로 전달된다. (재시도 가능)
     * [Book.id] 는 한 목록 안에서 겹치지 않는다.
     *
     * @param query 검색어. 공백뿐이면 서버를 부르지 않고 빈 목록을 준다.
     */
    fun searchBooks(query: String, sort: SearchSortType = SearchSortType.Accuracy): Flow<PagingData<Book>>
}
