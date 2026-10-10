package com.psk.mybooks.core.data

import androidx.paging.PagingSource
import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.BookApiException
import com.psk.mybooks.core.data.remote.BookSearchResponse
import com.psk.mybooks.core.data.remote.SearchSort
import com.psk.mybooks.core.data.remote.SearchTarget
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.ErrorReason
import com.psk.mybooks.core.model.ErrorSeverity
import com.psk.mybooks.core.model.toErrorReason
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BookPagingSourceTest {

    private class FakeBookApi(
        var onCall: (page: Int) -> BookSearchResponse
    ) : BookApi {
        var callCount: Int = 0
        var lastQuery: String? = null
        var lastSort: SearchSort? = null
        var lastPage: Int? = null
        var lastSize: Int? = null

        override suspend fun searchBooks(
            query: String,
            sort: SearchSort,
            page: Int,
            size: Int,
            target: SearchTarget?
        ): BookSearchResponse {
            callCount++
            lastQuery = query
            lastSort = sort
            lastPage = page
            lastSize = size
            return onCall(page)
        }
    }

    private fun document(bookId: Int): BookSearchResponse.BookDocument = BookSearchResponse.BookDocument(
        title = "책 $bookId",
        contents = "",
        url = "https://search.daum.net/search?w=bookpage&bookId=$bookId",
        isbn = "9772383695005",
        datetime = "",
        authors = emptyList(),
        publisher = "",
        translators = emptyList(),
        price = 1000,
        salePrice = -1,
        thumbnail = "",
        status = ""
    )

    private fun response(vararg ids: Int, isEnd: Boolean = false): BookSearchResponse = BookSearchResponse(
        BookSearchResponse.Meta(totalCount = ids.size, pageableCount = ids.size, isEnd = isEnd),
        ids.map { document(it) }
    )

    private fun source(api: BookApi, query: String = "한강", sort: SearchSort = SearchSort.Accuracy): BookPagingSource =
        BookPagingSource(api = api, query = query, sort = sort, pageSize = 10)

    private fun refresh(key: Int? = null): PagingSource.LoadParams<Int> = PagingSource.LoadParams
        .Refresh(key = key, loadSize = 10, placeholdersEnabled = false)

    private fun append(key: Int): PagingSource.LoadParams<Int> = PagingSource.LoadParams
        .Append(key = key, loadSize = 10, placeholdersEnabled = false)

    private fun PagingSource.LoadResult<Int, Book>.page(): PagingSource.LoadResult.Page<Int, Book> =
        this as PagingSource.LoadResult.Page

    @Test
    fun `처음에는 1페이지를 정해진 크기와 정렬로 불러오고 다음 키는 2다`() = runTest {
        val api: FakeBookApi = FakeBookApi { response(1, 2) }

        val result = source(api, query = " 한강 ", sort = SearchSort.Latest)
            .load(refresh())
            .page()

        assertEquals(
            listOf("1", "2"),
            result.data
                .map { it.id }
        )
        assertEquals(2, result.nextKey)
        assertNull(result.prevKey)
        assertEquals(1, api.lastPage)
        assertEquals(10, api.lastSize)
        assertEquals("한강", api.lastQuery)
        assertEquals(SearchSort.Latest, api.lastSort)
    }

    @Test
    fun `is_end 가 true 이면 다음 키가 없다`() = runTest {
        val result = source(FakeBookApi { response(1, isEnd = true) })
            .load(refresh())
            .page()

        assertNull(result.nextKey)
    }

    @Test
    fun `50페이지에서는 is_end 가 false 여도 다음 키가 없다`() = runTest {
        val result = source(FakeBookApi { response(1, isEnd = false) })
            .load(append(50))
            .page()

        assertNull(result.nextKey)
    }

    @Test
    fun `한 페이지 안의 같은 항목은 하나만 남긴다`() = runTest {
        val result = source(FakeBookApi { response(1, 2, 1) })
            .load(refresh())
            .page()

        assertEquals(
            listOf("1", "2"),
            result.data
                .map { it.id }
        )
    }

    @Test
    fun `앞 페이지에서 이미 보낸 항목은 다음 페이지에서 걸러 낸다`() = runTest {
        // 1페이지 [1,2,3] 을 받은 뒤 서버 결과가 밀려 2페이지가 [3,4,5] 로 온 경우
        val api: FakeBookApi = FakeBookApi { page -> if (page == 1) response(1, 2, 3) else response(3, 4, 5) }
        val source: BookPagingSource = source(api)

        source
            .load(refresh())
            .page()
        val second = source
            .load(append(2))
            .page()

        assertEquals(
            listOf("4", "5"),
            second.data
                .map { it.id }
        )
    }

    @Test
    fun `같은 페이지를 다시 불러오면(재시도) 항목을 걸러 내지 않는다`() = runTest {
        val source: BookPagingSource = source(FakeBookApi { response(1, 2) })

        source
            .load(refresh())
            .page()
        val again = source
            .load(refresh())
            .page()

        assertEquals(
            listOf("1", "2"),
            again.data
                .map { it.id }
        )
    }

    @Test
    fun `검색어가 비어 있으면 서버를 부르지 않고 빈 페이지를 준다`() = runTest {
        val api: FakeBookApi = FakeBookApi { error("호출되면 안 된다") }

        val result = source(api, query = "  ")
            .load(refresh())
            .page()

        assertEquals(0, api.callCount)
        assertTrue(
            result.data
                .isEmpty()
        )
        assertNull(result.nextKey)
    }

    @Test
    fun `네트워크 오류는 예외가 아니라 Network 로 분류한 Error 결과로 돌려준다`() = runTest {
        val error: IOException = IOException("offline")

        val result = source(FakeBookApi { throw error }).load(refresh())

        val thrown: Throwable = (result as PagingSource.LoadResult.Error).throwable
        assertEquals(ErrorReason.Network, thrown.toErrorReason())
        assertSame(error, thrown.cause)
    }

    @Test
    fun `시간 초과는 Timeout 이고 연결 실패나 끊김은 Network 로 분류한다`() = runTest {
        suspend fun reasonOf(error: Exception): ErrorReason =
            (source(FakeBookApi { throw error }).load(refresh()) as PagingSource.LoadResult.Error)
                .throwable
                .toErrorReason()

        assertEquals(ErrorReason.Timeout, reasonOf(SocketTimeoutException("read timed out")))
        assertEquals(ErrorReason.Timeout, reasonOf(SocketTimeoutException("connect timed out")))
        assertEquals(ErrorReason.Network, reasonOf(ConnectException("연결 거부")))
        assertEquals(ErrorReason.Network, reasonOf(UnknownHostException("dapi.kakao.com")))
        assertEquals(ErrorReason.Network, reasonOf(IOException("끊김")))
    }

    @Test
    fun `API 오류는 종류에 맞는 이유로 분류한다`() = runTest {
        suspend fun reasonOf(error: Exception): ErrorReason =
            (source(FakeBookApi { throw error }).load(refresh()) as PagingSource.LoadResult.Error)
                .throwable
                .toErrorReason()

        assertEquals(ErrorReason.Unauthorized, reasonOf(BookApiException.Unauthorized(401, null, "AccessDeniedError")))
        assertEquals(ErrorReason.Forbidden, reasonOf(BookApiException.Forbidden(403, -3, null)))
        assertEquals(ErrorReason.InvalidRequest, reasonOf(BookApiException.InvalidRequest(400, -2, null)))
        assertEquals(ErrorReason.QuotaExceeded, reasonOf(BookApiException.QuotaExceeded(429, null, null)))
        assertEquals(ErrorReason.Server, reasonOf(BookApiException.ServerError(500, null, null)))
        assertEquals(ErrorReason.Unknown, reasonOf(BookApiException.Unknown(418, null, null)))
        assertEquals(ErrorReason.Unknown, reasonOf(IllegalStateException("예상 밖")))
    }

    @Test
    fun `중요도는 인증과 권한 오류만 High 이고 나머지는 Low 다`() {
        val high: List<ErrorReason> = ErrorReason.entries
            .filter { it.severity == ErrorSeverity.High }

        assertEquals(listOf(ErrorReason.Unauthorized, ErrorReason.Forbidden), high)
    }

    @Test
    fun `코루틴 취소는 Error 로 삼키지 않고 다시 던진다`() {
        val api: FakeBookApi = FakeBookApi { throw CancellationException("cancelled") }

        assertThrows(CancellationException::class.java) {
            kotlinx.coroutines
                .runBlocking { source(api).load(refresh()) }
        }
    }
}
