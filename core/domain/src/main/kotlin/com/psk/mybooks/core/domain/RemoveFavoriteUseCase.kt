package com.psk.mybooks.core.domain

import com.psk.mybooks.core.data.FavoriteRepository
import javax.inject.Inject

/**
 * 즐겨찾기에서 도서를 뺀다. 담겨 있지 않은 id 면 아무 일도 없다.
 * 저장은 Room 의 suspend 함수라 호출한 스레드를 막지 않으므로 Dispatcher 를 따로 지정하지 않는다.
 */
class RemoveFavoriteUseCase @Inject constructor(
    private val repository: FavoriteRepository
) {

    suspend operator fun invoke(bookId: String): Unit = repository.remove(bookId)
}
