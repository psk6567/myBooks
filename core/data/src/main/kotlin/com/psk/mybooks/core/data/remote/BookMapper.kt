package com.psk.mybooks.core.data.remote

import com.psk.mybooks.core.domain.model.Book

/** 상세 URL 의 `bookId=숫자`. 도서 항목마다 하나씩이고 제목이 바뀌어도 변하지 않는다. */
private val BOOK_ID_REGEX: Regex = Regex("""bookId=(\d+)""")

/**
 * 도서 항목을 구분하는 id. 상세 URL 의 bookId 를 쓰고, 없으면 URL 전체, 그것도 없으면 ISBN 을 쓴다.
 * (ISBN 은 월간지처럼 호수가 달라도 같을 수 있어 1순위로 쓰지 않는다)
 */
private fun BookSearchResponse.BookDocument.bookId(): String =
    BOOK_ID_REGEX.find(url)?.groupValues?.get(1) ?: url.ifBlank { isbn }

internal fun BookSearchResponse.BookDocument.toBook(): Book = Book(
    id = bookId(),
    title = title,
    contents = contents,
    url = url,
    isbn = isbn,
    publishedAt = datetime,
    authors = authors,
    publisher = publisher,
    translators = translators,
    price = price,
    salePrice = salePrice,
    thumbnailUrl = thumbnail,
    status = status
)

/** 같은 항목이 한 번 더 오면 처음 것만 남긴다. (화면 목록의 key 가 겹치면 앱이 죽는다) */
internal fun BookSearchResponse.toBookPage(): BookPage = BookPage(
    books = documents.map { it.toBook() }.distinctBy { it.id },
    isEnd = meta.isEnd
)
