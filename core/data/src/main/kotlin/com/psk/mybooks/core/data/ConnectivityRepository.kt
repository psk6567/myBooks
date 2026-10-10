package com.psk.mybooks.core.data

/** 지금 인터넷에 연결되어 있는지 알려 준다. */
interface ConnectivityRepository {

    /** 인터넷에 닿을 수 있는 네트워크가 있으면 true. 연결됐다고 해서 서버가 응답한다는 뜻은 아니다. */
    fun isOnline(): Boolean
}
