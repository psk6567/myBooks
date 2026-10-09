package com.psk.mybooks.core.domain

import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import javax.inject.Inject

/**
 * 도서를 즐겨찾기에 담는다. 이미 담겨 있으면 최신 값으로 바꾼다.
 * 저장은 Room 의 suspend 함수라 호출한 스레드를 막지 않으므로 Dispatcher 를 따로 지정하지 않는다.
 */
class AddFavoriteUseCase @Inject constructor(private val repository: FavoriteRepository) {

    suspend operator fun invoke(book: Book): Unit = repository.add(book)
}
