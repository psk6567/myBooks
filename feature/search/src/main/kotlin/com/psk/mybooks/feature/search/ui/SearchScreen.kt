package com.psk.mybooks.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psk.mybooks.core.designsystem.component.BookGridItem
import com.psk.mybooks.core.designsystem.component.BookListItem
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.domain.model.Book
import com.psk.mybooks.feature.search.R
import java.text.NumberFormat

/**
 * ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다.
 *
 * @param isGrid 격자(true)/목록(false). 화면 표시 방식일 뿐이라 main 이 정해서 넘긴다.
 */
@Composable
fun SearchRoute(isGrid: Boolean, modifier: Modifier = Modifier) {
    val viewModel: SearchViewModel = viewModel()
    val state: SearchUiState by viewModel.state.collectAsStateWithLifecycle()

    SearchScreen(state = state, isGrid = isGrid, onIntent = viewModel::onIntent, modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun SearchScreen(
    state: SearchUiState,
    isGrid: Boolean,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isGrid) {
        BookGrid(state = state, onIntent = onIntent, modifier = modifier)
    } else {
        BookList(state = state, onIntent = onIntent, modifier = modifier)
    }
}

@Composable
private fun BookList(state: SearchUiState, onIntent: (SearchIntent) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.books, key = { it.id }) { book ->
            BookListItem(
                status = book.status,
                title = book.title,
                publisher = book.publisher,
                authors = book.authors.joinToString(", "),
                price = stringResource(R.string.feature_search_price, formatPrice(book.price)),
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                // 상세 화면 이동은 detail 을 만들 때 연결한다
                onClick = {},
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book.id)) }
            )
        }
    }
}

@Composable
private fun BookGrid(state: SearchUiState, onIntent: (SearchIntent) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.books, key = { it.id }) { book ->
            BookGridItem(
                title = book.title,
                publisher = book.publisher,
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                onClick = {},
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book.id)) }
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

private val previewState: SearchUiState = SearchUiState(
    books = listOf(
        previewBook(isbn = "1", title = "도서 제목", price = 18000, status = "정상판매"),
        previewBook(isbn = "2", title = "아주 긴 도서 제목이 들어가면 두 줄까지 보입니다", price = 1234000, status = "품절")
    ),
    favoriteIds = setOf("1")
)

@Preview(name = "List", widthDp = 360)
@Composable
private fun SearchScreenListPreview() {
    MyBooksTheme {
        SearchScreen(state = previewState, isGrid = false, onIntent = {})
    }
}

@Preview(name = "Grid", widthDp = 360)
@Composable
private fun SearchScreenGridPreview() {
    MyBooksTheme {
        SearchScreen(state = previewState, isGrid = true, onIntent = {})
    }
}
