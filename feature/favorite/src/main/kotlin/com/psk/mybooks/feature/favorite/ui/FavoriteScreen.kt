package com.psk.mybooks.feature.favorite.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psk.mybooks.core.designsystem.component.BookGridItem
import com.psk.mybooks.core.designsystem.component.BookListItem
import com.psk.mybooks.core.designsystem.component.DialogButtons
import com.psk.mybooks.core.designsystem.component.EmptyMessage
import com.psk.mybooks.core.designsystem.component.MbDialog
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.model.FavoriteSortType
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.core.ui.BookGridColumns
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.ErrorEffect
import com.psk.mybooks.core.ui.priceText
import com.psk.mybooks.core.ui.salePriceText
import com.psk.mybooks.feature.favorite.R

/**
 * ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다.
 *
 * @param sort 정렬 기준. main 의 칩 값이다.
 * @param priceFilter 금액 필터. main 의 칩 값이다.
 * @param isGrid 격자(true)/목록(false). 화면 표시 방식일 뿐이라 main 이 정해서 넘긴다.
 * @param onBookClick 도서를 눌러 상세 화면을 열 때. 화면 이동은 main 이 연결한다.
 */
@Composable
fun FavoriteRoute(
    sort: FavoriteSortType,
    priceFilter: PriceFilter,
    isGrid: Boolean,
    onBookClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: FavoriteViewModel = hiltViewModel()
    val state: FavoriteUiState by viewModel.state.collectAsStateWithLifecycle()

    // 오류를 중요도에 따라 스낵바나 다이얼로그로 알린다
    ErrorEffect(viewModel.error)
    // ViewModel 이 내보낸 이동 요청을 받아 main 에 알린다
    val currentOnBookClick: (BookUiModel) -> Unit by rememberUpdatedState(onBookClick)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FavoriteEffect.OpenDetail -> currentOnBookClick(effect.book)
            }
        }
    }

    // main 이 가진 정렬과 금액 필터를 이 화면의 상태로 맞춘다
    LaunchedEffect(sort, priceFilter) { viewModel.onIntent(FavoriteIntent.ChangeCondition(sort, priceFilter)) }

    FavoriteScreen(state = state, isGrid = isGrid, onIntent = viewModel::onIntent, modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun FavoriteScreen(
    state: FavoriteUiState,
    isGrid: Boolean,
    onIntent: (FavoriteIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    // 이 탭에서만 즐겨찾기를 빼기 전에 한 번 더 묻는다
    state.pendingRemoval?.let { book ->
        MbDialog(
            title = stringResource(R.string.feature_favorite_remove_title),
            message = stringResource(R.string.feature_favorite_remove_message, book.title),
            buttons = DialogButtons.Two(
                confirmText = stringResource(R.string.feature_favorite_remove_confirm),
                dismissText = stringResource(R.string.feature_favorite_remove_cancel),
                onConfirm = { onIntent(FavoriteIntent.ConfirmRemove) },
                onDismiss = { onIntent(FavoriteIntent.DismissRemove) }
            ),
            onDismissRequest = { onIntent(FavoriteIntent.DismissRemove) }
        )
    }
    if (state.books.isEmpty()) {
        // 필터 때문에 비었으면 즐겨찾기가 없는 것이 아니라 조건에 맞는 도서가 없는 것이다
        val message: Int =
            if (state.priceFilter.isActive) R.string.feature_favorite_no_match else R.string.feature_favorite_empty
        EmptyMessage(message = stringResource(message), modifier = modifier)
        return
    }
    if (isGrid) {
        BookGrid(state = state, onIntent = onIntent, modifier = modifier)
    } else {
        BookList(state = state, onIntent = onIntent, modifier = modifier)
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
                authors = book.authors,
                price = book.priceText(),
                // 정가와 판매가가 다를 때만 판매가를 따로 보여 준다
                salePrice = book.salePriceText(),
                thumbnailUrl = book.thumbnailUrl,
                // 즐겨찾기 목록의 책은 모두 즐겨찾기 상태다
                isFavorite = true,
                onClick = { onIntent(FavoriteIntent.OpenDetail(book)) },
                onFavoriteClick = { onIntent(FavoriteIntent.RequestRemove(book)) }
            )
        }
    }
}

@Composable
private fun BookGrid(state: FavoriteUiState, onIntent: (FavoriteIntent) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = BookGridColumns,
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
                price = book.priceText(),
                salePrice = book.salePriceText(),
                isFavorite = true,
                onClick = { onIntent(FavoriteIntent.OpenDetail(book)) },
                onFavoriteClick = { onIntent(FavoriteIntent.RequestRemove(book)) }
            )
        }
    }
}

private fun previewBook(id: String, title: String, price: Int, salePrice: Int?, status: String): BookUiModel =
    BookUiModel(
        id = id,
        status = status,
        title = title,
        contents = "",
        url = "",
        isbn = "",
        publishedAt = "",
        publisher = "출판사",
        authors = "저자",
        translators = "",
        price = price,
        salePrice = salePrice,
        thumbnailUrl = ""
    )

private val previewState: FavoriteUiState = FavoriteUiState(
    books = listOf(
        previewBook(id = "1", title = "도서 제목", price = 20000, salePrice = 18000, status = "정상판매"),
        previewBook(id = "2", title = "아주 긴 도서 제목이 들어가면 두 줄까지 보입니다", price = 1234000, salePrice = null, status = "품절")
    )
)

@Preview(name = "List", widthDp = 360)
@Composable
private fun FavoriteScreenListPreview() {
    MyBooksTheme {
        FavoriteScreen(state = previewState, isGrid = false, onIntent = {})
    }
}

@Preview(name = "Grid", widthDp = 360)
@Composable
private fun FavoriteScreenGridPreview() {
    MyBooksTheme {
        FavoriteScreen(state = previewState, isGrid = true, onIntent = {})
    }
}

@Preview(name = "Remove dialog", widthDp = 360, heightDp = 640)
@Composable
private fun FavoriteScreenRemoveDialogPreview() {
    MyBooksTheme {
        FavoriteScreen(
            state = previewState.copy(pendingRemoval = previewState.books.first()),
            isGrid = false,
            onIntent = {}
        )
    }
}

@Preview(name = "Empty", widthDp = 360, heightDp = 400)
@Composable
private fun FavoriteScreenEmptyPreview() {
    MyBooksTheme {
        FavoriteScreen(state = FavoriteUiState(), isGrid = false, onIntent = {})
    }
}
