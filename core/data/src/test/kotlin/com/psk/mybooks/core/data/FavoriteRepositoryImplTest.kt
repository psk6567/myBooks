package com.psk.mybooks.core.data

import com.psk.mybooks.core.data.local.FavoriteDao
import com.psk.mybooks.core.data.local.FavoriteEntity
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteRepositoryImplTest {

    /** 같은 id 는 덮어쓰고, 쿼리(`ORDER BY savedAt DESC, id`)대로 돌려주는 DB 처럼 동작하는 메모리 DAO */
    private class FakeFavoriteDao : FavoriteDao {
        val rows: MutableStateFlow<List<FavoriteEntity>> = MutableStateFlow(emptyList())

        override fun observeAll(): Flow<List<FavoriteEntity>> = rows.map { list ->
            list.sortedWith(compareByDescending<FavoriteEntity> { it.savedAt }.thenBy { it.id })
        }

        override fun observeIds(): Flow<List<String>> = rows.map { list -> list.map { it.id } }

        override suspend fun insert(entity: FavoriteEntity) {
            rows.value = rows.value
                .filterNot { it.id == entity.id } + entity
        }

        override suspend fun delete(id: String) {
            rows.value = rows.value
                .filterNot { it.id == id }
        }
    }

    private val dao: FakeFavoriteDao = FakeFavoriteDao()

    /** 담을 때마다 1ms 씩 흐르는 시계. 먼저 담은 것이 항상 더 이른 시각이 된다. */
    private var time: Long = 1_000L
    private val repository: FavoriteRepository = FavoriteRepositoryImpl(dao) { time++ }

    private fun book(id: String, title: String = "제목$id", price: Int = 10000, salePrice: Int = -1): Book = Book(
        id = id,
        title = title,
        contents = "소개",
        url = "https://search.daum.net/search?bookId=$id",
        isbn = "isbn$id",
        publishedAt = "2024-01-01T00:00:00.000+09:00",
        authors = listOf("저자, 쉼표 포함", "저자2"),
        publisher = "출판사",
        translators = listOf("번역자"),
        price = price,
        salePrice = salePrice,
        thumbnailUrl = "https://example.com/$id.jpg",
        status = "정상판매"
    )

    @Test
    fun `담은 도서는 모든 값이 그대로 돌아온다`() = runTest {
        val saved: Book = book("1")

        repository.add(saved)

        assertEquals(
            listOf(saved),
            repository
                .observeFavorites(FavoriteSortType.TitleAsc)
                .first()
        )
    }

    @Test
    fun `같은 도서를 다시 담으면 하나만 남고 최신 값이다`() = runTest {
        repository.add(book("1", price = 10000))
        repository.add(book("1", price = 12000))

        val favorites: List<Book> = repository
            .observeFavorites(FavoriteSortType.TitleAsc)
            .first()

        assertEquals(1, favorites.size)
        assertEquals(
            12000,
            favorites
                .single()
                .price
        )
    }

    @Test
    fun `담을 때 시각이 기록되고 다시 담으면 시각이 새로 기록된다`() = runTest {
        repository.add(book("1"))
        repository.add(book("2"))
        val first: Long = dao.rows.value
            .first { it.id == "1" }
            .savedAt

        repository.add(book("1"))

        assertEquals(1_000L, first)
        assertEquals(
            1_002L,
            dao.rows.value
                .first { it.id == "1" }
                .savedAt
        )
    }

    @Test
    fun `뺀 도서는 목록과 id 모음에서 사라지고 없는 id 를 빼도 아무 일 없다`() = runTest {
        repository.add(book("1"))
        repository.add(book("2"))

        repository.remove("1")
        repository.remove("없는 id")

        assertEquals(
            setOf("2"),
            repository
                .observeFavoriteIds()
                .first()
        )
        assertEquals(
            listOf("2"),
            repository
                .observeFavorites(FavoriteSortType.TitleAsc)
                .first()
                .map { it.id }
        )
    }

    @Test
    fun `아무것도 담지 않았으면 빈 목록이고 빈 id 모음이다`() = runTest {
        assertTrue(
            repository
                .observeFavorites(FavoriteSortType.TitleAsc)
                .first()
                .isEmpty()
        )
        assertTrue(
            repository
                .observeFavoriteIds()
                .first()
                .isEmpty()
        )
    }

    @Test
    fun `정렬 기준대로 순서가 정해진다`() = runTest {
        repository.add(book("3", title = "나", price = 20000))
        repository.add(book("1", title = "다", price = 10000))
        repository.add(book("2", title = "가", price = 20000))

        suspend fun ids(sort: FavoriteSortType): List<String> = repository
            .observeFavorites(sort)
            .first()
            .map { it.id }

        assertEquals(listOf("2", "3", "1"), ids(FavoriteSortType.TitleAsc))
        assertEquals(listOf("1", "3", "2"), ids(FavoriteSortType.TitleDesc))
        assertEquals(listOf("1", "2", "3"), ids(FavoriteSortType.PriceAsc))
        assertEquals(listOf("2", "3", "1"), ids(FavoriteSortType.PriceDesc))
    }

    @Test
    fun `가격 정렬은 판매가 기준이고 판매가를 모르면 정가로 정렬한다`() = runTest {
        repository.add(book("1", price = 50000, salePrice = 9000))
        repository.add(book("2", price = 20000, salePrice = -1))
        repository.add(book("3", price = 12000, salePrice = 30000))

        suspend fun ids(sort: FavoriteSortType): List<String> = repository
            .observeFavorites(sort)
            .first()
            .map { it.id }

        assertEquals(listOf("1", "2", "3"), ids(FavoriteSortType.PriceAsc))
        assertEquals(listOf("3", "2", "1"), ids(FavoriteSortType.PriceDesc))
    }

    @Test
    fun `정렬 기준이 같으면 최근에 담은 것이 먼저다`() = runTest {
        repository.add(book("1", price = 10000))
        repository.add(book("2", price = 10000))
        repository.add(book("3", price = 10000))

        assertEquals(
            listOf("3", "2", "1"),
            repository.observeFavorites(FavoriteSortType.PriceAsc).first().map {
                it.id
            }
        )

        repository.add(book("1", price = 10000))

        assertEquals(
            listOf("1", "3", "2"),
            repository.observeFavorites(FavoriteSortType.PriceDesc).first().map {
                it.id
            }
        )
    }

    @Test
    fun `금액 필터는 최소와 최대 금액을 포함해서 그 사이의 도서만 남긴다`() = runTest {
        repository.add(book("1", price = 5000))
        repository.add(book("2", price = 10000))
        repository.add(book("3", price = 20000))
        repository.add(book("4", price = 30000))

        suspend fun ids(filter: PriceFilter): List<String> = repository
            .observeFavorites(FavoriteSortType.PriceAsc, filter)
            .first()
            .map { it.id }

        assertEquals(listOf("1", "2", "3", "4"), ids(PriceFilter()))
        assertEquals(listOf("2", "3", "4"), ids(PriceFilter(min = 10000)))
        assertEquals(listOf("1", "2", "3"), ids(PriceFilter(max = 20000)))
        assertEquals(listOf("2", "3"), ids(PriceFilter(min = 10000, max = 20000)))
        assertEquals(listOf("2"), ids(PriceFilter(min = 10000, max = 10000)))
    }

    @Test
    fun `금액 필터는 판매가로 거르고 판매가를 모르면 정가로 거른다`() = runTest {
        repository.add(book("1", price = 50000, salePrice = 9000))
        repository.add(book("2", price = 8000, salePrice = -1))
        repository.add(book("3", price = 8000, salePrice = 30000))

        val ids: List<String> = repository
            .observeFavorites(FavoriteSortType.TitleAsc, PriceFilter(max = 10000))
            .first()
            .map { it.id }

        assertEquals(listOf("1", "2"), ids)
    }

    @Test
    fun `금액 필터에 맞는 도서가 없으면 빈 목록이고 최소가 최대보다 크면 모두 걸러진다`() = runTest {
        repository.add(book("1", price = 10000))

        assertTrue(
            repository
                .observeFavorites(FavoriteSortType.TitleAsc, PriceFilter(min = 20000))
                .first()
                .isEmpty()
        )
        assertTrue(
            repository
                .observeFavorites(FavoriteSortType.TitleAsc, PriceFilter(min = 20000, max = 5000))
                .first()
                .isEmpty()
        )
    }

    @Test
    fun `금액 필터를 건 목록도 정렬 기준대로 나온다`() = runTest {
        repository.add(book("1", title = "다", price = 10000))
        repository.add(book("2", title = "가", price = 20000))
        repository.add(book("3", title = "나", price = 30000))

        val ids: List<String> = repository
            .observeFavorites(FavoriteSortType.TitleDesc, PriceFilter(max = 20000))
            .first()
            .map { it.id }

        assertEquals(listOf("1", "2"), ids)
    }

    @Test
    fun `담거나 빼면 구독 중인 id 모음이 바로 갱신된다`() = runTest {
        val seen: MutableList<Set<String>> = mutableListOf()
        val ids: Flow<Set<String>> = repository.observeFavoriteIds()

        seen += ids.first()
        repository.add(book("1"))
        seen += ids.first()
        repository.remove("1")
        seen += ids.first()

        assertEquals(listOf(emptySet(), setOf("1"), emptySet()), seen)
    }
}
