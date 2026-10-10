package com.psk.mybooks.core.data.di

import android.content.Context
import androidx.room.Room
import com.psk.mybooks.core.data.local.FavoriteDao
import com.psk.mybooks.core.data.local.MyBooksDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME: String = "mybooks.db"

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyBooksDatabase = Room
        .databaseBuilder(context, MyBooksDatabase::class.java, DATABASE_NAME)
        .build()

    @Provides
    fun provideFavoriteDao(database: MyBooksDatabase): FavoriteDao = database.favoriteDao()
}
