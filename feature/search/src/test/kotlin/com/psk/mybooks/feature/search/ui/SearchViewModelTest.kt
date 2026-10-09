package com.psk.mybooks.feature.search.ui

import androidx.paging.PagingData
import com.psk.mybooks.core.data.BookRepository
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.domain.AddFavoriteUseCase
import com.psk.mybooks.core.domain.FetchSearchBooksUseCase
import com.psk.mybooks.core.domain.ObserveFavoriteIdsUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.SearchSortType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule(dispatcher)

    private class FakeBookRepository : BookRepository {
        val calls: MutableList<Pair<String, SearchSortType>> = mutableListOf()

        override fun searchBooks(query: String, sort: SearchSortType): Flow<PagingData<Book>> {
            calls += query to sort
            return flowOf(PagingData.empty())
        }
    }

    /** 담은 도서를 들고 있는 메모리 즐겨찾기 저장소 */
    private class FakeFavoriteRepository : FavoriteRepository {
        val books: MutableStateFlow<List<Book>> = MutableStateFlow(emptyList())

        override fun observeFavorites(sort: FavoriteSortType): Flow<List<Book>> = books

        override fun observeFavoriteIds(): Flow<Set<String>> = books.map { list -> list.map { it.id }.toSet() }

        override suspend fun add(book: Book) {
            books.value = books.value.filterNot { it.id == book.id } + book
        }

        override suspend fun remove(bookId: String) {
            books.value = books.value.filterNot { it.id == bookId }
        }
    }

    private fun book(id: String): Book = Book(
        id = id,
        title = "제목$id",
        contents = "",
        url = "",
        isbn = "",
        publishedAt = "",
        authors = emptyList(),
        publisher = "",
        translators = emptyList(),
        price = 0,
        salePrice = -1,
        thumbnailUrl = "",
        status = ""
    )

    private fun viewModel(
        repository: FakeBookRepository,
        favorites: FakeFavoriteRepository = FakeFavoriteRepository()
    ): SearchViewModel = SearchViewModel(
        FetchSearchBooksUseCase(repository, dispatcher),
        ObserveFavoriteIdsUseCase(favorites, dispatcher),
        AddFavoriteUseCase(favorites, dispatcher),
        RemoveFavoriteUseCase(favorites, dispatcher)
    )

    @Test
    fun `검색어는 입력이 멈춘 뒤 300ms 후에 검색한다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch { viewModel.books.collect {} }

        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(299)
        assertFalse(repository.calls.any { it.first == "한강" })

        advanceTimeBy(2)
        assertTrue(repository.calls.contains("한강" to SearchSortType.Accuracy))
    }

    @Test
    fun `입력 중에는 마지막 검색어만 검색한다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch { viewModel.books.collect {} }

        viewModel.onIntent(SearchIntent.ChangeCondition("한", SearchSortType.Accuracy))
        advanceTimeBy(100)
        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(400)

        assertEquals(listOf("한강"), repository.calls.map { it.first })
    }

    @Test
    fun `정렬을 바꾸면 기다리지 않고 바로 다시 검색한다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch { viewModel.books.collect {} }
        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(301)

        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Latest))
        runCurrent()

        assertEquals("한강" to SearchSortType.Latest, repository.calls.last())
    }

    @Test
    fun `앞뒤 공백만 다른 검색어는 다시 검색하지 않는다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch { viewModel.books.collect {} }
        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(301)
        val before: Int = repository.calls.size

        viewModel.onIntent(SearchIntent.ChangeCondition("한강 ", SearchSortType.Accuracy))
        advanceTimeBy(400)

        assertEquals(before, repository.calls.size)
    }

    @Test
    fun `하트를 누르면 즐겨찾기에 저장되어 켜지고 다시 누르면 꺼진다`() {
        val favorites: FakeFavoriteRepository = FakeFavoriteRepository()
        val viewModel: SearchViewModel = viewModel(FakeBookRepository(), favorites)

        viewModel.onIntent(SearchIntent.ToggleFavorite(book("1")))
        assertEquals(listOf("1"), favorites.books.value.map { it.id })
        assertEquals(setOf("1"), viewModel.state.value.favoriteIds)

        viewModel.onIntent(SearchIntent.ToggleFavorite(book("1")))
        assertEquals(emptyList<Book>(), favorites.books.value)
        assertEquals(emptySet<String>(), viewModel.state.value.favoriteIds)
    }

    @Test
    fun `다른 화면에서 담거나 빼도 하트가 바로 반영된다`() {
        val favorites: FakeFavoriteRepository = FakeFavoriteRepository()
        val viewModel: SearchViewModel = viewModel(FakeBookRepository(), favorites)

        favorites.books.value = listOf(book("1"), book("2"))
        assertEquals(setOf("1", "2"), viewModel.state.value.favoriteIds)

        favorites.books.value = listOf(book("2"))
        assertEquals(setOf("2"), viewModel.state.value.favoriteIds)
    }
}
