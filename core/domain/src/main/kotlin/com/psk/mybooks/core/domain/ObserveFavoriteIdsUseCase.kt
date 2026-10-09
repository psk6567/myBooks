package com.psk.mybooks.core.domain

import com.psk.mybooks.core.common.IoDispatcher
import com.psk.mybooks.core.data.FavoriteRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

/**
 * 즐겨찾기한 도서의 id 모음을 계속 받아 본다. 검색 결과나 상세 화면이 하트를 그릴 때 쓴다.
 * 읽는 작업은 [ioDispatcher] 에서 실행한다.
 */
class ObserveFavoriteIdsUseCase @Inject constructor(
    private val repository: FavoriteRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Set<String>> = repository.observeFavoriteIds().flowOn(ioDispatcher)
}
