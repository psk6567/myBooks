package com.psk.mybooks.feature.favorite.ui

import com.psk.mybooks.core.data.FavoriteRepository
import com.psk.mybooks.core.domain.ObserveFavoritesUseCase
import com.psk.mybooks.core.domain.RemoveFavoriteUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.ui.BookUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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

        /** 설정하면 빼기가 이 예외로 실패한다 */
        var failure: Exception? = null

        override fun observeFavorites(
            sort: FavoriteSortType,
            priceFilter: PriceFilter,
            query: String
        ): Flow<List<Book>> {
            requestedSorts += sort
            return books.map { all ->
                val list: List<Book> = all.filter {
                    priceFilter.matches(it.price) && it.title
                        .contains(query.trim())
                }
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
            failure?.let { throw it }
            books.value = books.value
                .filterNot { it.id == bookId }
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

        assertEquals(
            listOf("1", "2"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `정렬을 바꾸면 그 순서로 다시 받는다`() {
        val viewModel: FavoriteViewModel = viewModel(
            FakeFavoriteRepository(listOf(book("1", "가", price = 30000), book("2", "나", price = 10000)))
        )

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.PriceAsc, PriceFilter(), ""))

        assertEquals(FavoriteSortType.PriceAsc, viewModel.state.value.sort)
        assertEquals(
            listOf("2", "1"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `검색어를 넣으면 맞는 도서만 남고 지우면 다시 모두 나온다`() {
        val viewModel: FavoriteViewModel = viewModel(
            FakeFavoriteRepository(listOf(book("1", "소설 모음"), book("2", "에세이"), book("3", "한국 소설")))
        )

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter(), "소설"))
        assertEquals("소설", viewModel.state.value.query)
        assertEquals(
            listOf("1", "3"),
            viewModel.state.value.books
                .map { it.id }
        )

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter(), ""))
        assertEquals(
            listOf("1", "2", "3"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `금액 필터를 걸면 맞는 도서만 남고 풀면 다시 모두 나온다`() {
        val viewModel: FavoriteViewModel = viewModel(
            FakeFavoriteRepository(
                listOf(book("1", "가", price = 5000), book("2", "나", price = 15000), book("3", "다", price = 30000))
            )
        )

        viewModel.onIntent(
            FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter(min = 10000, max = 20000), "")
        )
        assertEquals(PriceFilter(min = 10000, max = 20000), viewModel.state.value.priceFilter)
        assertEquals(
            listOf("2"),
            viewModel.state.value.books
                .map { it.id }
        )

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter(), ""))
        assertEquals(
            listOf("1", "2", "3"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `같은 정렬을 다시 받아도 다시 불러오지 않는다`() {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository(emptyList())
        val viewModel: FavoriteViewModel = viewModel(repository)

        viewModel.onIntent(FavoriteIntent.ChangeCondition(FavoriteSortType.TitleAsc, PriceFilter(), ""))

        assertEquals(listOf(FavoriteSortType.TitleAsc), repository.requestedSorts)
    }

    private fun FavoriteViewModel.requestRemove(id: String) {
        onIntent(
            FavoriteIntent.RequestRemove(
                state.value.books
                    .first { it.id == id }
            )
        )
    }

    @Test
    fun `하트를 누르면 바로 빼지 않고 삭제 확인을 기다린다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"), book("2", "나"))))

        viewModel.requestRemove("1")

        assertEquals(
            "1",
            viewModel.state.value.pendingRemoval
                ?.id
        )
        assertEquals(
            listOf("1", "2"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `삭제를 확인하면 그 도서만 목록에서 빠지고 다이얼로그가 닫힌다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"), book("2", "나"))))

        viewModel.requestRemove("1")
        viewModel.onIntent(FavoriteIntent.ConfirmRemove)

        assertEquals(
            listOf("2"),
            viewModel.state.value.books
                .map { it.id }
        )
        assertNull(viewModel.state.value.pendingRemoval)
    }

    @Test
    fun `삭제를 취소하면 도서는 그대로이고 다이얼로그가 닫힌다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"))))

        viewModel.requestRemove("1")
        viewModel.onIntent(FavoriteIntent.DismissRemove)

        assertEquals(
            listOf("1"),
            viewModel.state.value.books
                .map { it.id }
        )
        assertNull(viewModel.state.value.pendingRemoval)
    }

    @Test
    fun `확인할 도서가 없는데 삭제를 확인해도 아무 일도 없다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"))))

        viewModel.onIntent(FavoriteIntent.ConfirmRemove)

        assertEquals(
            listOf("1"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `모두 확인해서 빼면 목록이 비어 있다`() {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"), book("2", "나"))))

        listOf("1", "2").forEach { id ->
            viewModel.requestRemove(id)
            viewModel.onIntent(FavoriteIntent.ConfirmRemove)
        }

        assertTrue(
            viewModel.state.value.books
                .isEmpty()
        )
    }

    @Test
    fun `삭제를 확인했는데 빼다 실패하면 오류를 알리고 목록은 그대로다`() = runTest(dispatcher) {
        val repository: FakeFavoriteRepository = FakeFavoriteRepository(listOf(book("1", "가"))).apply {
            failure = IllegalStateException("디스크 오류")
        }
        val viewModel: FavoriteViewModel = viewModel(repository)
        val received: MutableList<Throwable> = mutableListOf()
        backgroundScope.launch {
            viewModel.error
                .collect { received += it }
        }

        viewModel.requestRemove("1")
        viewModel.onIntent(FavoriteIntent.ConfirmRemove)

        assertEquals(listOf("디스크 오류"), received.map { it.message })
        assertEquals(
            listOf("1"),
            viewModel.state.value.books
                .map { it.id }
        )
    }

    @Test
    fun `도서를 누르면 상세 화면으로 이동하라는 요청을 한 번 내보낸다`() = runTest(dispatcher) {
        val viewModel: FavoriteViewModel = viewModel(FakeFavoriteRepository(listOf(book("1", "가"))))
        val received: MutableList<FavoriteEffect> = mutableListOf()
        backgroundScope.launch {
            viewModel.effect
                .collect { received += it }
        }
        val ui: BookUiModel = viewModel.state.value.books
            .first()

        viewModel.onIntent(FavoriteIntent.OpenDetail(ui))

        assertEquals(listOf<FavoriteEffect>(FavoriteEffect.OpenDetail(ui)), received)
    }
}
