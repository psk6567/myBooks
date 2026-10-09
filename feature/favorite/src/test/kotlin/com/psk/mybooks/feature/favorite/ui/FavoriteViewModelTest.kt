package com.psk.mybooks.feature.favorite.ui

import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.domain.ObserveFavoritesUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule(dispatcher)

    /** 담은 도서를 들고 있고 정렬 기준과 뺀 id 를 기록하는 메모리 Repository */
    private class FakeFavoriteRepository(
        initial: List<Book>
    ) : FavoriteRepository {
        val books: MutableStateFlow<List<Book>> = MutableStateFlow(initial)
        val requestedSorts: MutableList<FavoriteSortType> = mutableListOf()

        override fun observeFavorites(sort: FavoriteSortType, priceFilter: PriceFilter): Flow<List<Book>> {
            requestedSorts += sort
            return books.map { all ->
                val list: List<Book> = all.filter { priceFilter.matches(it.price) }
                when (sort) {
                    FavoriteSortType.TitleAsc -> list.sortedBy { it.title }
                    FavoriteSortType.TitleDesc -> list.sortedByDescending { it.title }
                    FavoriteSortType.PriceAsc -> list.sortedBy { it.price }
                    FavoriteSortType.PriceDesc -> list.sortedByDescending { it.price }
                }
            }
        }

        override fun observeFavoriteIds(): Flow<Set<String>> = emptyFlow()

        override suspend fun add(book: Book) = Unit

        override suspend fun remove(bookId: String) {
            books.value = books.value.filterNot { it.id == bookId }
        }
    }

    private fun book(id: String, title: String, price: Int = 10000): Book = Book(
        id = id,
        title = title,
        contents = "",
        url = "",
        isbn = "",
        publishedAt = "",
        authors = emptyList(),
        publisher = "",
        translators = emptyList(),
        price = price,
        salePrice = -1,
        thumbnailUrl = "",
        status = ""
    )

    private fun viewModel(repository: FakeFavoriteRepository): FavoriteViewModel = FavoriteViewModel(
        ObserveFavoritesUseCase(repository, dispatcher),
        RemoveFavoriteUseCase(repository, dispatcher)
    )

    @Test
    fun `시작하면 저장된 즐겨찾기 목록이 제목순으로 채워진다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("2", "나"), book("1", "가"))))

        assertEquals(listOf("1", "2"), viewModel.state.value.books.map { it.id })
    }

    @Test
    fun `정렬을 바꾸면 그 순서로 다시 받는다`() {
        val viewModel: FavoriteViewModel = viewModel(
            FakeFavoriteRepository(listOf(book("1", "가", price = 30000), book("2", "나", price = 10000)))
        )

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.PriceAsc, PriceFilter()))

        assertEquals(FavoriteSortType.PriceAsc, viewModel.state.value.sort)
        assertEquals(listOf("2", "1"), viewModel.state.value.books.map { it.id })
    }

    @Test
    fun `금액 필터를 걸면 맞는 도서만 남고 풀면 다시 모두 나온다`() {
        val viewModel: FavoriteViewModel = viewModel(
            FakeFavoriteRepository(
                listOf(book("1", "가", price = 5000), book("2", "나", price = 15000), book("3", "다", price = 30000))
            )
        )

        viewModel.onIntent(
            FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter(min = 10000, max = 20000))
        )
        assertEquals(PriceFilter(min = 10000, max = 20000), viewModel.state.value.priceFilter)
        assertEquals(listOf("2"), viewModel.state.value.books.map { it.id })

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter()))
        assertEquals(listOf("1", "2", "3"), viewModel.state.value.books.map { it.id })
    }

    @Test
    fun `같은 정렬을 다시 받아도 다시 불러오지 않는다`() {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository(emptyList())
        val viewModel: FavoriteViewModel = viewModel(repository)

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter()))

        assertEquals(listOf(FavoriteSortType.TitleAsc), repository.requestedSorts)
    }

    @Test
    fun `하트를 누르면 그 도서만 목록에서 빠진다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"), book("2", "나"))))

        viewModel.onIntent(FavoriteIntent.RemoveFavorite("1"))

        assertEquals(listOf("2"), viewModel.state.value.books.map { it.id })
    }

    @Test
    fun `모두 빼면 목록이 비어 있다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"), book("2", "나"))))

        viewModel.state.value.books.map { it.id }.forEach { viewModel.onIntent(FavoriteIntent.RemoveFavorite(it)) }

        assertTrue(viewModel.state.value.books.isEmpty())
    }
}
