package com.psk.mybooks.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.psk.mybooks.core.designsystem.component.BookGridItem
import com.psk.mybooks.core.designsystem.component.BookListItem
import com.psk.mybooks.core.designsystem.component.EmptyMessage
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.model.isOnSale
import com.psk.mybooks.feature.search.R
import java.text.NumberFormat
import kotlinx.coroutines.flow.flowOf

/**
 * ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다.
 *
 * @param query 검색어. main 의 검색창 값이다.
 * @param sort 정렬 기준. main 의 칩 값이다.
 * @param isGrid 격자(true)/목록(false). 화면 표시 방식일 뿐이라 main 이 정해서 넘긴다.
 */
@Composable
fun SearchRoute(query: String, sort: SearchSortType, isGrid: Boolean, modifier: Modifier = Modifier) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state: SearchUiState by viewModel.state.collectAsStateWithLifecycle()
    val books: LazyPagingItems<Book> = viewModel.books.collectAsLazyPagingItems()

    // main 이 가진 검색어와 정렬을 이 화면의 상태로 맞춘다
    LaunchedEffect(query, sort) { viewModel.onIntent(SearchIntent.ChangeCondition(query, sort)) }

    SearchScreen(state = state, books = books, isGrid = isGrid, onIntent = viewModel::onIntent, modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun SearchScreen(
    state: SearchUiState,
    books: LazyPagingItems<Book>,
    isGrid: Boolean,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // 검색어가 없으면 불러올 목록이 없으므로 안내 문구를 보여 준다
    if (state.query.isBlank()) {
        EmptyMessage(message = stringResource(R.string.feature_search_empty_query), modifier = modifier)
        return
    }
    // 검색을 마쳤는데 결과가 없으면 안내 문구를 보여 준다
    if (books.loadState.isNoResult(books.itemCount)) {
        EmptyMessage(message = stringResource(R.string.feature_search_no_result), modifier = modifier)
        return
    }
    if (isGrid) {
        BookGrid(state = state, books = books, onIntent = onIntent, modifier = modifier)
    } else {
        BookList(state = state, books = books, onIntent = onIntent, modifier = modifier)
    }
}

@Composable
private fun BookList(
    state: SearchUiState,
    books: LazyPagingItems<Book>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count = books.itemCount, key = books.itemKey { it.id }) { index ->
            val book: Book = books[index] ?: return@items
            BookListItem(
                status = book.status,
                title = book.title,
                publisher = book.publisher,
                authors = book.authors.joinToString(", "),
                price = stringResource(R.string.feature_search_price, formatPrice(book.price)),
                // 정가와 판매가가 다를 때만 판매가를 따로 보여 준다
                salePrice = if (book.isOnSale) {
                    stringResource(R.string.feature_search_price, formatPrice(book.salePrice))
                } else {
                    null
                },
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                // 상세 화면 이동은 detail 을 만들 때 연결한다
                onClick = {},
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book)) }
            )
        }
    }
}

@Composable
private fun BookGrid(
    state: SearchUiState,
    books: LazyPagingItems<Book>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(count = books.itemCount, key = books.itemKey { it.id }) { index ->
            val book: Book = books[index] ?: return@items
            BookGridItem(
                title = book.title,
                publisher = book.publisher,
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                onClick = {},
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book)) }
            )
        }
    }
}

/** 천 단위 구분 (예: 15000 → 15,000) */
private fun formatPrice(price: Int): String = NumberFormat.getNumberInstance().format(price)

private fun previewBook(isbn: String, title: String, price: Int, status: String): Book = Book(
    id = isbn,
    title = title,
    contents = "",
    url = "",
    isbn = isbn,
    publishedAt = "",
    authors = listOf("저자"),
    publisher = "출판사",
    translators = emptyList(),
    price = price,
    salePrice = price,
    thumbnailUrl = "",
    status = status
)

private val previewBooks: List<Book> = listOf(
    previewBook(isbn = "1", title = "도서 제목", price = 18000, status = "정상판매"),
    previewBook(isbn = "2", title = "아주 긴 도서 제목이 들어가면 두 줄까지 보입니다", price = 1234000, status = "품절")
)

private val previewState: SearchUiState = SearchUiState(query = "도서", favoriteIds = setOf("1"))

@Composable
private fun previewItems(): LazyPagingItems<Book> = flowOf(PagingData.from(previewBooks)).collectAsLazyPagingItems()

@Preview(name = "List", widthDp = 360)
@Composable
private fun SearchScreenListPreview() {
    MyBooksTheme {
        SearchScreen(state = previewState, books = previewItems(), isGrid = false, onIntent = {})
    }
}

@Preview(name = "Grid", widthDp = 360)
@Composable
private fun SearchScreenGridPreview() {
    MyBooksTheme {
        SearchScreen(state = previewState, books = previewItems(), isGrid = true, onIntent = {})
    }
}

@Preview(name = "No query", widthDp = 360, heightDp = 400)
@Composable
private fun SearchScreenNoQueryPreview() {
    MyBooksTheme {
        SearchScreen(state = SearchUiState(), books = previewItems(), isGrid = false, onIntent = {})
    }
}
