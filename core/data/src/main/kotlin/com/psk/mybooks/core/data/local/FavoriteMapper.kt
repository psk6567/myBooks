package com.psk.mybooks.core.data.local

import com.psk.mybooks.core.model.Book

internal fun Book.toEntity(savedAt: Long): FavoriteEntity = FavoriteEntity(
    id = id,
    title = title,
    contents = contents,
    url = url,
    isbn = isbn,
    publishedAt = publishedAt,
    authors = authors,
    publisher = publisher,
    translators = translators,
    price = price,
    salePrice = salePrice,
    thumbnailUrl = thumbnailUrl,
    status = status,
    savedAt = savedAt
)

internal fun FavoriteEntity.toBook(): Book = Book(
    id = id,
    title = title,
    contents = contents,
    url = url,
    isbn = isbn,
    publishedAt = publishedAt,
    authors = authors,
    publisher = publisher,
    translators = translators,
    price = price,
    salePrice = salePrice,
    thumbnailUrl = thumbnailUrl,
    status = status
)
