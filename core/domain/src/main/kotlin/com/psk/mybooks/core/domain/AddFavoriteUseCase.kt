package com.psk.mybooks.core.domain

import com.psk.mybooks.core.common.IoDispatcher
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * 도서를 즐겨찾기에 담는다. 이미 담겨 있으면 최신 값으로 바꾼다.
 * 저장은 [ioDispatcher] 에서 실행한다. 그래서 viewModelScope(Main)에서 불러도 스레드를 신경 쓸 필요가 없다.
 */
class AddFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(book: Book): Unit = withContext(ioDispatcher) { repository.add(book) }
}
