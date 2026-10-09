package com.psk.mybooks.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface FavoriteDao {

    /** 최근에 담은 것부터. 시각이 같으면 id 순이라 순서가 매번 같다. */
    @Query("SELECT * FROM favorite_books ORDER BY savedAt DESC, id")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT id FROM favorite_books")
    fun observeIds(): Flow<List<String>>

    /** 같은 id 가 이미 있으면 최신 값으로 덮어쓴다. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorite_books WHERE id = :id")
    suspend fun delete(id: String)
}
