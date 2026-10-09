package com.psk.mybooks.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FavoriteEntity::class], version = 1)
@TypeConverters(StringListConverter::class)
internal abstract class MyBooksDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao
}
