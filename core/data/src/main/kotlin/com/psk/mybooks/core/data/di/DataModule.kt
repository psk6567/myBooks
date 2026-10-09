package com.psk.mybooks.core.data.di

import com.psk.mybooks.core.data.BookRepository
import com.psk.mybooks.core.data.BookRepositoryImpl
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.data.FavoriteRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Repository 인터페이스에 구현을 연결한다. 구현은 internal 이라 이 모듈 밖에서는 볼 수 없다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository

    @Binds
    abstract fun bindFavoriteRepository(impl: FavoriteRepositoryImpl): FavoriteRepository
}
