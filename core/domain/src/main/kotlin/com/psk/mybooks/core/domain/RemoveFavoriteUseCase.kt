package com.psk.mybooks.core.domain

import com.psk.mybooks.core.common.IoDispatcher
import com.psk.mybooks.core.data.FavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 즐겨찾기에서 도서를 뺀다. 담겨 있지 않은 id 면 아무 일도 없다.
 * 저장은 [ioDispatcher] 에서 실행한다. 그래서 viewModelScope(Main)에서 불러도 스레드를 신경 쓸 필요가 없다.
 */
class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(bookId: String): Unit = withContext(ioDispatcher) { repository.remove(bookId) }
}
