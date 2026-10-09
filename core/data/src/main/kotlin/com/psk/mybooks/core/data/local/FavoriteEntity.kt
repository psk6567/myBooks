package com.psk.mybooks.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 즐겨찾기한 도서. 오프라인에서도 목록을 보여줄 수 있도록 [com.psk.mybooks.core.model.Book] 전체를 저장한다. */
@Entity(tableName = "favorite_books")
internal data class FavoriteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val contents: String,
    val url: String,
    val isbn: String,
    val publishedAt: String,
    val authors: List<String>,
    val publisher: String,
    val translators: List<String>,
    val price: Int,
    val salePrice: Int,
    val thumbnailUrl: String,
    val status: String,
    /** 즐겨찾기에 담은 시각(epoch millis). 같은 값끼리는 최근에 담은 것을 앞에 둘 때 쓴다. */
    val savedAt: Long
)
