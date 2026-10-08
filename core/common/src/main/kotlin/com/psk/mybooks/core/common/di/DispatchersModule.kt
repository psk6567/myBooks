package com.psk.mybooks.core.common.di

import com.psk.mybooks.core.common.DefaultDispatcher
import com.psk.mybooks.core.common.IoDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Dispatcher 를 직접 쓰지 않고 주입받게 해서, UseCase 가 스레드를 정하고 ViewModel 은 모르게 한다.
 * 테스트에서는 테스트용 Dispatcher 를 넣는다.
 */
@Module
@InstallIn(SingletonComponent::class)
internal object DispatchersModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
