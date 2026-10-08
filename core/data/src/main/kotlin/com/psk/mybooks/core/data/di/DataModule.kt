package com.psk.mybooks.core.data.di

import com.psk.mybooks.core.data.BookRepositoryImpl
import com.psk.mybooks.core.domain.BookRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** 도메인의 인터페이스에 데이터 계층의 구현을 연결한다. feature 는 구현을 볼 수 없다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindBookRepository(impl: BookRepositoryImpl): BookRepository
}
