package com.psk.mybooks.core.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.SearchSort
import com.psk.mybooks.core.data.remote.SearchTarget
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.model.SearchTargetType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/** 한 번에 불러오는 문서 수. API 는 1~50 이다. */
internal const val SEARCH_PAGE_SIZE: Int = 20

internal class BookRepositoryImpl @Inject constructor(
    private val api: BookApi
) : BookRepository {

    override fun searchBooks(query: String, sort: SearchSortType, target: SearchTargetType): Flow<PagingData<Book>> =
        Pager(
            config = PagingConfig(
                pageSize = SEARCH_PAGE_SIZE,
                // API 가 쪽수(page)와 크기(size)로 나뉘므로 첫 로드도 한 페이지 크기여야 쪽수 계산이 맞는다
                initialLoadSize = SEARCH_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                BookPagingSource(api, query, sort.toApiSort(), SEARCH_PAGE_SIZE, target.toApiTarget())
            }
        ).flow
}

private fun SearchSortType.toApiSort(): SearchSort = when (this) {
    SearchSortType.Accuracy -> SearchSort.Accuracy
    SearchSortType.Latest -> SearchSort.Latest
}

/** 제한 없음은 서버에 보내지 않는다(null). 저자는 API 에서 인명(person)이다. */
private fun SearchTargetType.toApiTarget(): SearchTarget? = when (this) {
    SearchTargetType.All -> null
    SearchTargetType.Title -> SearchTarget.Title
    SearchTargetType.Isbn -> SearchTarget.Isbn
    SearchTargetType.Publisher -> SearchTarget.Publisher
    SearchTargetType.Author -> SearchTarget.Person
}
