package com.psk.mybooks.feature.search.ui

import androidx.paging.PagingData
import com.psk.mybooks.core.domain.BookRepository
import com.psk.mybooks.core.domain.FetchSearchBooksUseCase
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
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

    private fun viewModel(repository: FakeBookRepository): SearchViewModel =
        SearchViewModel(FetchSearchBooksUseCase(repository, dispatcher))

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
    fun `하트를 누르면 즐겨찾기가 켜지고 다시 누르면 꺼진다`() {
        val viewModel: SearchViewModel = viewModel(FakeBookRepository())

        viewModel.onIntent(SearchIntent.ToggleFavorite("1"))
        assertEquals(setOf("1"), viewModel.state.value.favoriteIds)

        viewModel.onIntent(SearchIntent.ToggleFavorite("1"))
        assertEquals(emptySet<String>(), viewModel.state.value.favoriteIds)
    }
}
