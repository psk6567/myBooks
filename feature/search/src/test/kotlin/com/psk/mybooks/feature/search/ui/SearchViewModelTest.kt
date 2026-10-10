package com.psk.mybooks.feature.search.ui

import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.psk.mybooks.core.data.BookRepository
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.domain.AddFavoriteUseCase
import com.psk.mybooks.core.domain.FetchSearchBooksUseCase
import com.psk.mybooks.core.domain.ObserveFavoriteIdsUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.AppException
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.ErrorReason
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.toUiModel
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
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

    private class FakeBookRepository(
        private val results: List<Book> = emptyList()
    ) : BookRepository {
        val calls: MutableList<Pair<String, SearchSortType>> = mutableListOf()

        override fun searchBooks(query: String, sort: SearchSortType): Flow<PagingData<Book>> {
            calls += query to sort
            return flowOf(if (results.isEmpty()) PagingData.empty() else PagingData.from(results))
        }
    }

    /** 담은 도서를 들고 있는 메모리 즐겨찾기 저장소 */
    private class FakeFavoriteRepository : FavoriteRepository {
        val books: MutableStateFlow<List<Book>> = MutableStateFlow(emptyList())

        /** 설정하면 담기·빼기가 이 예외로 실패한다 */
        var failure: Exception? = null

        override fun observeFavorites(
            sort: FavoriteSortType,
            priceFilter: PriceFilter,
            query: String
        ): Flow<List<Book>> = books

        override fun observeFavoriteIds(): Flow<Set<String>> = books.map { list ->
            list
                .map { it.id }
                .toSet()
        }

        override suspend fun add(book: Book) {
            failure?.let { throw it }
            books.value = books.value
                .filterNot { it.id == book.id } + book
        }

        override suspend fun remove(bookId: String) {
            failure?.let { throw it }
            books.value = books.value
                .filterNot { it.id == bookId }
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

    private class UiPresenter(
        context: CoroutineContext
    ) : PagingDataPresenter<BookUiModel>(context) {
        override suspend fun presentPagingDataEvent(event: PagingDataEvent<BookUiModel>) = Unit
    }

    /**
     * 화면(LazyPagingItems)처럼 검색 결과를 수집해 보여 줄 도서를 돌려준다. 변환은 이렇게 수집될 때 실행되므로,
     * 이때 ViewModel 이 원본을 기억한다. 하트는 이렇게 화면에 내보낸 뒤에 누를 수 있다.
     */
    private fun TestScope.load(viewModel: SearchViewModel, query: String): List<BookUiModel> {
        val presenter: UiPresenter = UiPresenter(dispatcher)
        backgroundScope.launch {
            viewModel.books
                .collectLatest { presenter.collectFrom(it) }
        }
        viewModel.onIntent(SearchIntent.ChangeCondition(query, SearchSortType.Accuracy))
        advanceTimeBy(301)
        runCurrent()
        return presenter
            .snapshot()
            .items
    }

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
        backgroundScope.launch {
            viewModel.books
                .collect {}
        }

        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(299)
        assertFalse(
            repository.calls
                .any { it.first == "한강" }
        )

        advanceTimeBy(2)
        assertTrue(
            repository.calls
                .contains("한강" to SearchSortType.Accuracy)
        )
    }

    @Test
    fun `입력 중에는 마지막 검색어만 검색한다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch {
            viewModel.books
                .collect {}
        }

        viewModel.onIntent(SearchIntent.ChangeCondition("한", SearchSortType.Accuracy))
        advanceTimeBy(100)
        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(400)

        assertEquals(
            listOf("한강"),
            repository.calls
                .map { it.first }
        )
    }

    @Test
    fun `정렬을 바꾸면 기다리지 않고 바로 다시 검색한다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch {
            viewModel.books
                .collect {}
        }
        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(301)

        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Latest))
        runCurrent()

        assertEquals(
            "한강" to SearchSortType.Latest,
            repository.calls
                .last()
        )
    }

    @Test
    fun `앞뒤 공백만 다른 검색어는 다시 검색하지 않는다`() = runTest(dispatcher) {
        val repository: FakeBookRepository = FakeBookRepository()
        val viewModel: SearchViewModel = viewModel(repository)
        backgroundScope.launch {
            viewModel.books
                .collect {}
        }
        viewModel.onIntent(SearchIntent.ChangeCondition("한강", SearchSortType.Accuracy))
        advanceTimeBy(301)
        val before: Int = repository.calls.size

        viewModel.onIntent(SearchIntent.ChangeCondition("한강 ", SearchSortType.Accuracy))
        advanceTimeBy(400)

        assertEquals(before, repository.calls.size)
    }

    @Test
    fun `화면에는 도메인 모델이 아니라 표시용 값(UiModel)으로 내보낸다`() = runTest(dispatcher, timeout = 10.seconds) {
        val viewModel: SearchViewModel = viewModel(FakeBookRepository(listOf(book("1"))))

        val shown: List<BookUiModel> = load(viewModel, "한강")

        assertEquals(listOf(book("1").toUiModel()), shown)
    }

    @Test
    fun `하트를 누르면 불러온 원본이 즐겨찾기에 저장되어 켜지고 다시 누르면 꺼진다`() = runTest(dispatcher, timeout = 10.seconds) {
        val favorites: FakeFavoriteRepository = FakeFavoriteRepository()
        val viewModel: SearchViewModel = viewModel(FakeBookRepository(listOf(book("1"))), favorites)
        load(viewModel, "한강")

        viewModel.onIntent(SearchIntent.ToggleFavorite("1"))
        // 화면은 id 만 넘겼지만 저장된 것은 원본(Book) 전체다
        assertEquals(listOf(book("1")), favorites.books.value)
        assertEquals(setOf("1"), viewModel.state.value.favoriteIds)

        viewModel.onIntent(SearchIntent.ToggleFavorite("1"))
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

    @Test
    fun `하트를 저장하다 실패하면 오류를 알리고 하트는 그대로다`() = runTest(dispatcher, timeout = 10.seconds) {
        val favorites: FakeFavoriteRepository = FakeFavoriteRepository().apply {
            failure = IllegalStateException("디스크 가득")
        }
        val viewModel: SearchViewModel = viewModel(FakeBookRepository(listOf(book("1"))), favorites)
        load(viewModel, "한강")
        val received: MutableList<Throwable> = mutableListOf()
        backgroundScope.launch {
            viewModel.error
                .collect { received += it }
        }

        viewModel.onIntent(SearchIntent.ToggleFavorite("1"))

        assertEquals(listOf("디스크 가득"), received.map { it.message })
        assertEquals(emptySet<String>(), viewModel.state.value.favoriteIds)
    }

    @Test
    fun `다시 시도할 수 있는 불러오기 오류는 목록에서 안내하므로 스낵바로 또 알리지 않는다`() = runTest(dispatcher) {
        val viewModel: SearchViewModel = viewModel(FakeBookRepository())
        val received: MutableList<Throwable> = mutableListOf()
        backgroundScope.launch {
            viewModel.error
                .collect { received += it }
        }

        viewModel.onIntent(SearchIntent.ReportLoadError(AppException(ErrorReason.Network)))
        viewModel.onIntent(SearchIntent.ReportLoadError(AppException(ErrorReason.Timeout)))
        viewModel.onIntent(SearchIntent.ReportLoadError(AppException(ErrorReason.Server)))
        viewModel.onIntent(SearchIntent.ReportLoadError(Exception("분류되지 않은 오류")))

        assertTrue(received.isEmpty())
    }

    @Test
    fun `인증과 권한 오류는 사용자가 확인해야 하므로 알리고 같은 오류는 한 번만 전달한다`() = runTest(dispatcher) {
        val viewModel: SearchViewModel = viewModel(FakeBookRepository())
        val received: MutableList<Throwable> = mutableListOf()
        backgroundScope.launch {
            viewModel.error
                .collect { received += it }
        }
        val unauthorized: AppException = AppException(ErrorReason.Unauthorized)
        val forbidden: AppException = AppException(ErrorReason.Forbidden)

        viewModel.onIntent(SearchIntent.ReportLoadError(unauthorized))
        viewModel.onIntent(SearchIntent.ReportLoadError(unauthorized))
        viewModel.onIntent(SearchIntent.ReportLoadError(forbidden))

        assertEquals(listOf<Throwable>(unauthorized, forbidden), received)
    }

    @Test
    fun `도서를 누르면 상세 화면으로 이동하라는 요청을 한 번 내보낸다`() = runTest(dispatcher) {
        val viewModel: SearchViewModel = viewModel(FakeBookRepository())
        val received: MutableList<SearchEffect> = mutableListOf()
        backgroundScope.launch {
            viewModel.effect
                .collect { received += it }
        }
        val ui: BookUiModel = book("1").toUiModel()

        viewModel.onIntent(SearchIntent.OpenDetail(ui))

        assertEquals(listOf<SearchEffect>(SearchEffect.OpenDetail(ui)), received)
    }
}
