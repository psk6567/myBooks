package com.psk.mybooks.core.domain

import com.psk.mybooks.core.data.ConnectivityRepository
import javax.inject.Inject

/** 지금 인터넷에 연결되어 있는지 확인한다. 앱을 처음 열 때 검색을 쓸 수 있는지 판단하는 데 쓴다. */
class IsOnlineUseCase @Inject constructor(
    private val repository: ConnectivityRepository
) {

    operator fun invoke(): Boolean = repository.isOnline()
}
