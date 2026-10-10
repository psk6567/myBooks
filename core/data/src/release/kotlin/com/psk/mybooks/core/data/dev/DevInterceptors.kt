package com.psk.mybooks.core.data.dev

import okhttp3.Interceptor

/** release 에는 개발자 모드가 없어 붙일 인터셉터가 없다. (debug 소스셋에 같은 이름의 실제 구현이 있다) */
internal fun devInterceptors(): List<Interceptor> = emptyList()
