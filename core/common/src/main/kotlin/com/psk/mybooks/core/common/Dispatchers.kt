package com.psk.mybooks.core.common

import javax.inject.Qualifier

/** 네트워크·디스크처럼 기다리는 작업을 하는 Dispatcher */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/** 계산이 많은 작업을 하는 Dispatcher */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher
