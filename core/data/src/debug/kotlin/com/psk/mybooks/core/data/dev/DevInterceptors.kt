package com.psk.mybooks.core.data.dev

import okhttp3.Interceptor

/** 도서 검색 API 클라이언트에 붙일 개발자 모드용 인터셉터. debug 빌드에서만 있고, release 에서는 비어 있다. */
internal fun devInterceptors(): List<Interceptor> = listOf(DevFaultInterceptor())
