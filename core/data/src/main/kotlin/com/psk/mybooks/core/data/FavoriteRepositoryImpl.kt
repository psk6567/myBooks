package com.psk.mybooks.core.data

import com.psk.mybooks.core.data.local.FavoriteDao
import com.psk.mybooks.core.data.local.toBook
import com.psk.mybooks.core.data.local.toEntity
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FavoriteRepositoryImpl(
    private val dao: FavoriteDao,
    /** 담은 시각을 정하는 시계. 테스트에서 시각을 고정하려고 바꿔 끼울 수 있다. */
    private val now: () -> Long
) : FavoriteRepository {

    @Inject
    constructor(dao: FavoriteDao) : this(dao, System::currentTimeMillis)

    override fun observeFavorites(sort: FavoriteSortType, priceFilter: PriceFilter): Flow<List<Book>> =
        dao.observeAll().map { entities ->
            entities.filter { priceFilter.matches(it.price) }.map { it.toBook() }.sortedWith(sort.comparator())
        }

    override fun observeFavoriteIds(): Flow<Set<String>> = dao.observeIds().map { ids -> ids.toSet() }

    override suspend fun add(book: Book): Unit = dao.insert(book.toEntity(savedAt = now()))

    override suspend fun remove(bookId: String): Unit = dao.delete(bookId)
}

/**
 * 같은 값끼리는 DAO 가 준 순서(최근에 담은 순)를 그대로 둔다. sortedWith 는 순서를 바꾸지 않는 정렬이다.
 * 가격은 목록에 보이는 정가(price)다.
 */
private fun FavoriteSortType.comparator(): Comparator<Book> = when (this) {
    FavoriteSortType.TitleAsc -> compareBy<Book> { it.title }
    FavoriteSortType.TitleDesc -> compareByDescending<Book> { it.title }
    FavoriteSortType.PriceAsc -> compareBy<Book> { it.price }
    FavoriteSortType.PriceDesc -> compareByDescending<Book> { it.price }
}
