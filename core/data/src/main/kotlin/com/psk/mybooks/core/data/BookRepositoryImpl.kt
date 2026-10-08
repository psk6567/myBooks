package com.psk.mybooks.core.data

import com.psk.mybooks.core.data.remote.BookApi
import com.psk.mybooks.core.data.remote.toBooks
import com.psk.mybooks.core.model.Book
import kotlin.coroutines.cancellation.CancellationException

internal class BookRepositoryImpl(private val api: BookApi) : BookRepository {

    override suspend fun searchBooks(query: String): Result<List<Book>> {
        // 빈 검색어는 서버가 400 을 돌려주므로 부르지 않는다
        if (query.isBlank()) return Result.success(emptyList())

        return runCatchingCancellable { api.searchBooks(query = query.trim()).toBooks() }
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
