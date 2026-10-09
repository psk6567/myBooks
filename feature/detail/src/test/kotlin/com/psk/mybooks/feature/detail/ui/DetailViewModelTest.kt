package com.psk.mybooks.feature.detail.ui

import androidx.lifecycle.SavedStateHandle
import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.domain.AddFavoriteUseCase
import com.psk.mybooks.core.domain.ObserveFavoriteIdsUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.toBook
import com.psk.mybooks.core.ui.toUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val mainDispatcherRule: MainDispatcherRule = MainDispatcherRule(dispatcher)

    /** 담은 도서를 들고 있는 메모리 즐겨찾기 저장소 */
    private class FakeFavoriteRepository(
        initial: List<Book> = emptyList()
    ) : FavoriteRepository {
        val books: MutableStateFlow<List<Book>> = MutableStateFlow(initial)

        /** 설정하면 담기·빼기가 이 예외로 실패한다 */
        var failure: Exception? = null

        override fun observeFavorites(sort: FavoriteSortType, priceFilter: PriceFilter): Flow<List<Book>> = emptyFlow()

        override fun observeFavoriteIds(): Flow<Set<String>> = books.map { list -> list.map { it.id }.toSet() }

        override suspend fun add(book: Book) {
            failure?.let { throw it }
            books.value = books.value.filterNot { it.id == book.id } + book
        }

        override suspend fun remove(bookId: String) {
            failure?.let { throw it }
            books.value = books.value.filterNot { it.id == bookId }
        }
    }

    private val book: BookUiModel = BookUiModel(
        id = "7",
        status = "정상판매",
        title = "제목",
        contents = "소개",
        url = "https://search.daum.net/search?bookId=7",
        isbn = "isbn",
        publishedAt = "2024-01-01",
        publisher = "출판사",
        authors = "저자1, 저자2",
        translators = "",
        price = 20000,
        salePrice = 18000,
        thumbnailUrl = "https://example.com/7.jpg"
    )

    /** Navigation 이 이동 인자를 SavedStateHandle 에 담는 것과 같은 모양(속성 이름 `book` 에 JSON)으로 만든다. */
    private fun viewModel(repository: FakeFavoriteRepository): DetailViewModel = DetailViewModel(
        SavedStateHandle(mapOf("book" to Json.encodeToString(book))),
        ObserveFavoriteIdsUseCase(repository, dispatcher),
        AddFavoriteUseCase(repository, dispatcher),
        RemoveFavoriteUseCase(repository, dispatcher)
    )

    @Test
    fun `이동 인자로 받은 도서를 보여 주고 즐겨찾기가 아니면 하트가 꺼져 있다`() {
        val viewModel: DetailViewModel = viewModel(FakeFavoriteRepository())

        assertEquals(book, viewModel.state.value.book)
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `이미 즐겨찾기한 도서면 하트가 켜져 있다`() {
        val viewModel: DetailViewModel = viewModel(FakeFavoriteRepository(listOf(book.toBook())))

        assertTrue(viewModel.state.value.isFavorite)
    }

    @Test
    fun `하트를 누르면 도서가 즐겨찾기에 저장되어 켜지고 다시 누르면 꺼진다`() {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val viewModel: DetailViewModel = viewModel(repository)

        viewModel.onIntent(DetailIntent.ToggleFavorite)
        assertEquals(listOf(book.toBook()), repository.books.value)
        assertTrue(viewModel.state.value.isFavorite)

        viewModel.onIntent(DetailIntent.ToggleFavorite)
        assertEquals(emptyList<Book>(), repository.books.value)
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `저장한 도서를 다시 화면용으로 바꾸면 보던 도서와 같다`() {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val viewModel: DetailViewModel = viewModel(repository)

        viewModel.onIntent(DetailIntent.ToggleFavorite)

        assertEquals(book, repository.books.value.single().toUiModel())
    }

    @Test
    fun `다른 화면에서 즐겨찾기를 바꿔도 하트가 따라간다`() {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository()
        val viewModel: DetailViewModel = viewModel(repository)

        repository.books.value = listOf(book.toBook())
        assertTrue(viewModel.state.value.isFavorite)

        repository.books.value = emptyList()
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `저장하다 실패하면 오류를 알리고 하트는 그대로다`() = runTest(dispatcher) {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository().apply {
            failure = IllegalStateException("디스크 가득")
        }
        val viewModel: DetailViewModel = viewModel(repository)
        val received: MutableList<Throwable> = mutableListOf()
        backgroundScope.launch { viewModel.error.collect { received += it } }

        viewModel.onIntent(DetailIntent.ToggleFavorite)

        assertEquals(listOf("디스크 가득"), received.map { it.message })
        assertFalse(viewModel.state.value.isFavorite)
    }

    @Test
    fun `뒤로가기를 누르면 화면을 닫으라는 요청을 한 번 내보낸다`() = runTest(dispatcher) {
        val viewModel: DetailViewModel = viewModel(FakeFavoriteRepository())
        val received: MutableList<DetailEffect> = mutableListOf()
        backgroundScope.launch { viewModel.effect.collect { received += it } }

        viewModel.onIntent(DetailIntent.Back)

        assertEquals(listOf<DetailEffect>(DetailEffect.Close), received)
    }
}
