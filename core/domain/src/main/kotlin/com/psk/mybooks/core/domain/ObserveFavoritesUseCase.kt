package com.psk.mybooks.core.domain

import com.psk.mybooks.core.common.IoDispatcher
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * 즐겨찾기 목록을 [PriceFilter] 로 거르고 [FavoriteSortType] 순서로 계속 받아 본다. 담거나 빼면 새 목록이 흘러온다.
 * 읽고 거르고 정렬하는 작업은 [ioDispatcher] 에서 실행한다.
 */
class ObserveFavoritesUseCase @Inject constructor(
    private val repository: FavoriteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke(sort: FavoriteSortType, priceFilter: PriceFilter = PriceFilter()): Flow<List<Book>> =
        repository.observeFavorites(sort, priceFilter).flowOn(ioDispatcher)
}
