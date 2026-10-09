package com.psk.mybooks.core.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.BookPage
import com.psk.mybooks.core.data.remote.SearchSort
import com.psk.mybooks.core.data.remote.toBookPage
import com.psk.mybooks.core.model.Book
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

/** 카카오 API 가 허용하는 마지막 페이지 */
private const val MAX_PAGE: Int = 50

/**
 * 검색 결과를 페이지 단위로 불러온다. 검색어나 정렬이 바뀌면 새로 만든다.
 *
 * - 키는 페이지 번호(1부터)이고 한 페이지의 크기는 [pageSize] 로 고정이다. API 가 쪽수(page)와 크기(size)로 나누기 때문에
 *   Paging 의 `initialLoadSize` 도 [pageSize] 로 맞춰야 한다. (기본값은 3배라 쪽수 계산이 어긋난다)
 * - 다음 페이지는 응답의 `is_end` 가 false 이고 50페이지 전일 때만 있다.
 * - 서버 결과가 불러오는 사이에 바뀌면 페이지 경계의 항목이 다음 페이지에 또 올 수 있다.
 *   화면 목록의 key 가 겹치면 앱이 죽으므로, 앞선 다른 페이지에서 이미 보낸 항목은 걸러 낸다.
 */
internal class BookPagingSource(
    private val api: BookApi,
    private val query: String,
    private val sort: SearchSort,
    private val pageSize: Int
) : PagingSource<Int, Book>() {

    /** 이미 내보낸 항목의 id → 그 항목을 내보낸 페이지. 같은 페이지를 다시 불러오는 것(재시도)은 걸러 내지 않는다. */
    private val sentPages: ConcurrentHashMap<String, Int> = ConcurrentHashMap()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        // 빈 검색어는 결과가 없으므로 서버를 부르지 않는다. (서버도 200 과 빈 목록을 준다)
        if (query.isBlank()) return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)

        val page: Int = params.key ?: 1
        return try {
            val fetched: BookPage = api.searchBooks(query = query.trim(), sort = sort, page = page, size = pageSize)
                .toBookPage()
            val books: List<Book> = fetched.books.filter { book ->
                sentPages.putIfAbsent(book.id, page) in
                    listOf(null, page)
            }
            LoadResult.Page(
                data = books,
                prevKey = null,
                nextKey = if (fetched.isEnd || page >= MAX_PAGE) null else page + 1
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    // 검색 결과는 항상 처음부터 다시 불러온다
    override fun getRefreshKey(state: PagingState<Int, Book>): Int? = null
}
