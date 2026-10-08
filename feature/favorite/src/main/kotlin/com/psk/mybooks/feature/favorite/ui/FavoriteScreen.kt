package com.psk.mybooks.feature.favorite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.psk.mybooks.core.designsystem.component.BookGridItem
import com.psk.mybooks.core.designsystem.component.BookListItem
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.model.Book
import com.psk.mybooks.core.model.ViewMode
import com.psk.mybooks.feature.favorite.R
import java.text.NumberFormat

/**
 * ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다.
 *
 * @param viewMode 격자/목록. 화면 표시 방식일 뿐이라 main 이 정해서 넘긴다.
 */
@Composable
fun FavoriteRoute(viewMode: ViewMode, modifier: Modifier = Modifier) {
    val viewModel: FavoriteViewModel = viewModel()
    val state: FavoriteUiState by viewModel.state.collectAsStateWithLifecycle()

    FavoriteScreen(state = state, viewMode = viewMode, onIntent = viewModel::onIntent, modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun FavoriteScreen(
    state: FavoriteUiState,
    viewMode: ViewMode,
    onIntent: (FavoriteIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.books.isEmpty()) {
        EmptyFavorites(modifier = modifier)
        return
    }
    when (viewMode) {
        ViewMode.List -> BookList(state = state, onIntent = onIntent, modifier = modifier)
        ViewMode.Grid -> BookGrid(state = state, modifier = modifier)
    }
}

@Composable
private fun EmptyFavorites(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.feature_favorite_empty),
            style = MyBooksTheme.typography.normal16,
            color = MyBooksTheme.colors.textTertiary
        )
    }
}

@Composable
private fun BookList(state: FavoriteUiState, onIntent: (FavoriteIntent) -> Unit, modifier: Modifier = Modifier) {
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
                price = stringResource(R.string.feature_favorite_price, formatPrice(book.price)),
                thumbnailUrl = book.thumbnailUrl,
                // 즐겨찾기 목록의 책은 모두 즐겨찾기 상태다
                isFavorite = true,
                // 상세 화면 이동은 detail 을 만들 때 연결한다
                onClick = {},
                onFavoriteClick = { onIntent(FavoriteIntent.RemoveFavorite(book.id)) }
            )
        }
    }
}

@Composable
private fun BookGrid(state: FavoriteUiState, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(state.books, key = { it.id }) { book ->
            BookGridItem(title = book.title, publisher = book.publisher, thumbnailUrl = book.thumbnailUrl, onClick = {})
        }
    }
}

/** 천 단위 구분 (예: 15000 → 15,000) */
private fun formatPrice(price: Int): String = NumberFormat.getNumberInstance().format(price)

private val previewState: FavoriteUiState = FavoriteUiState(
    books = listOf(
        Book("1", "도서 제목", listOf("저자"), "출판사", 18000, "정상판매", ""),
        Book("2", "아주 긴 도서 제목이 들어가면 두 줄까지 보입니다", listOf("저자 A", "저자 B"), "출판사", 1234000, "품절", "")
    )
)

@Preview(name = "List", widthDp = 360)
@Composable
private fun FavoriteScreenListPreview() {
    MyBooksTheme {
        FavoriteScreen(state = previewState, viewMode = ViewMode.List, onIntent = {})
    }
}

@Preview(name = "Grid", widthDp = 360)
@Composable
private fun FavoriteScreenGridPreview() {
    MyBooksTheme {
        FavoriteScreen(state = previewState, viewMode = ViewMode.Grid, onIntent = {})
    }
}

@Preview(name = "Empty", widthDp = 360, heightDp = 400)
@Composable
private fun FavoriteScreenEmptyPreview() {
    MyBooksTheme {
        FavoriteScreen(state = FavoriteUiState(), viewMode = ViewMode.List, onIntent = {})
    }
}
