package com.psk.mybooks.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.psk.mybooks.core.designsystem.component.BookGridItem
import com.psk.mybooks.core.designsystem.component.BookListItem
import com.psk.mybooks.core.designsystem.component.EmptyMessage
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookGridColumns
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.ErrorEffect
import com.psk.mybooks.core.ui.priceText
import com.psk.mybooks.core.ui.salePriceText
import com.psk.mybooks.feature.search.R
import kotlinx.coroutines.flow.flowOf

/**
 * ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다.
 *
 * @param query 검색어. main 의 검색창 값이다.
 * @param sort 정렬 기준. main 의 칩 값이다.
 * @param isGrid 격자(true)/목록(false). 화면 표시 방식일 뿐이라 main 이 정해서 넘긴다.
 * @param onBookClick 도서를 눌러 상세 화면을 열 때. 화면 이동은 main 이 연결한다.
 */
@Composable
fun SearchRoute(
    query: String,
    sort: SearchSortType,
    isGrid: Boolean,
    onBookClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state: SearchUiState by viewModel.state.collectAsStateWithLifecycle()
    val books: LazyPagingItems<BookUiModel> = viewModel.books.collectAsLazyPagingItems()

    // 오류를 중요도에 따라 스낵바나 다이얼로그로 알린다
    ErrorEffect(viewModel.error)
    // ViewModel 이 내보낸 이동 요청을 받아 main 에 알린다
    val currentOnBookClick: (BookUiModel) -> Unit by rememberUpdatedState(onBookClick)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.OpenDetail -> currentOnBookClick(effect.book)
            }
        }
    }
    // 불러오기 실패(처음이든 다음 페이지든)는 화면의 로드 상태로만 알 수 있으므로 ViewModel 에 알린다
    val loadStates: CombinedLoadStates = books.loadState
    val loadError: Throwable? = (loadStates.refresh as? LoadState.Error)?.error
        ?: (loadStates.append as? LoadState.Error)?.error
    LaunchedEffect(loadError) { loadError?.let { viewModel.onIntent(SearchIntent.ReportLoadError(it)) } }

    // main 이 가진 검색어와 정렬을 이 화면의 상태로 맞춘다
    LaunchedEffect(query, sort) { viewModel.onIntent(SearchIntent.ChangeCondition(query, sort)) }

    SearchScreen(state = state, books = books, isGrid = isGrid, onIntent = viewModel::onIntent, modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun SearchScreen(
    state: SearchUiState,
    books: LazyPagingItems<BookUiModel>,
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
    books: LazyPagingItems<BookUiModel>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(count = books.itemCount, key = books.itemKey { it.id }) { index ->
            val book: BookUiModel = books[index] ?: return@items
            BookListItem(
                status = book.status,
                title = book.title,
                publisher = book.publisher,
                authors = book.authors,
                price = book.priceText(),
                // 정가와 판매가가 다를 때만 판매가를 따로 보여 준다
                salePrice = book.salePriceText(),
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                onClick = { onIntent(SearchIntent.OpenDetail(book)) },
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book.id)) }
            )
        }
    }
}

@Composable
private fun BookGrid(
    state: SearchUiState,
    books: LazyPagingItems<BookUiModel>,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = BookGridColumns,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(count = books.itemCount, key = books.itemKey { it.id }) { index ->
            val book: BookUiModel = books[index] ?: return@items
            BookGridItem(
                title = book.title,
                publisher = book.publisher,
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                onClick = { onIntent(SearchIntent.OpenDetail(book)) },
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book.id)) }
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

private val previewBooks: List<BookUiModel> = listOf(
    previewBook(id = "1", title = "도서 제목", price = 20000, salePrice = 18000, status = "정상판매"),
    previewBook(id = "2", title = "아주 긴 도서 제목이 들어가면 두 줄까지 보입니다", price = 1234000, salePrice = null, status = "품절")
)

private val previewState: SearchUiState = SearchUiState(query = "도서", favoriteIds = setOf("1"))

@Composable
private fun previewItems(): LazyPagingItems<BookUiModel> =
    flowOf(PagingData.from(previewBooks)).collectAsLazyPagingItems()

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
