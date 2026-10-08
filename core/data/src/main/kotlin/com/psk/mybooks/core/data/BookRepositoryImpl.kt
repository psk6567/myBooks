package com.psk.mybooks.core.data

import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.toBookPage
import com.psk.mybooks.core.model.BookPage
import kotlin.coroutines.cancellation.CancellationException

private val PAGINATION_RANGE: IntRange = 1..50

internal class BookRepositoryImpl(private val api: BookApi) : BookRepository {

    override suspend fun searchBooks(query: String, page: Int, size: Int): Result<BookPage> {
        require(page in PAGINATION_RANGE) { "page 는 1~50 이어야 한다: $page" }
        require(size in PAGINATION_RANGE) { "size 는 1~50 이어야 한다: $size" }
        // 빈 검색어는 서버가 400 을 돌려주므로 부르지 않는다
        if (query.isBlank()) return Result.success(BookPage(books = emptyList(), isEnd = true))

        return runCatchingCancellable { api.searchBooks(query = query.trim(), page = page, size = size).toBookPage() }
    }
}

/** `runCatching` 은 코루틴 취소(CancellationException)까지 삼켜 버려서, 취소는 다시 던진다. */
private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
