package com.psk.mybooks.core.domain

import com.psk.mybooks.core.common.IoDispatcher
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * 즐겨찾기 목록을 [FavoriteSortType] 순서로 계속 받아 본다. 담거나 빼면 새 목록이 흘러온다.
 * 읽고 정렬하는 작업은 [ioDispatcher] 에서 실행한다.
 */
class ObserveFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke(sort: FavoriteSortType): Flow<List<Book>> =
        repository.observeFavorites(sort).flowOn(ioDispatcher)
}
