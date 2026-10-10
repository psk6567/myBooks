package com.psk.mybooks.feature.search.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import com.psk.mybooks.core.designsystem.component.BookGridItemSkeleton
import com.psk.mybooks.core.designsystem.component.BookListItem
import com.psk.mybooks.core.designsystem.component.BookListItemSkeleton
import com.psk.mybooks.core.designsystem.component.EmptyMessage
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.model.SearchSortType
import com.psk.mybooks.core.ui.BookGridColumns
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.ErrorEffect
import com.psk.mybooks.core.ui.ListErrorView
import com.psk.mybooks.core.ui.ListFooterError
import com.psk.mybooks.core.ui.priceText
import com.psk.mybooks.core.ui.salePriceText
import com.psk.mybooks.feature.search.R
import kotlinx.coroutines.flow.flowOf

/** 자리표시를 몇 개 그릴지. 화면을 채우고 남는 만큼은 잘리므로 넉넉히 둔다. */
private const val SKELETON_COUNT: Int = 12

/**
 * ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다.
 *
 * @param query 검색어. main 의 검색창 값이다.
 * @param sort 정렬 기준. main 의 칩 값이다.
 * @param isGrid 격자(true)/목록(false). 화면 표시 방식일 뿐이라 main 이 정해서 넘긴다.
 * @param onBookClick 도서를 눌러 상세 화면을 열 때. 화면 이동은 main 이 연결한다.
 * @param selectedBookId 넓은 화면에서 옆 패널에 상세가 열려 있는 도서의 id. 목록에서 강조한다. 없으면 강조하지 않는다.
 */
@Composable
fun SearchRoute(
    query: String,
    sort: SearchSortType,
    isGrid: Boolean,
    onBookClick: (BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
    selectedBookId: String? = null
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val state: SearchUiState by viewModel.state
        .collectAsStateWithLifecycle()
    val books: LazyPagingItems<BookUiModel> = viewModel.books
        .collectAsLazyPagingItems()

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

    SearchScreen(
        state = state,
        books = books,
        isGrid = isGrid,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        selectedBookId = selectedBookId
    )
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun SearchScreen(
    state: SearchUiState,
    books: LazyPagingItems<BookUiModel>,
    isGrid: Boolean,
    onIntent: (SearchIntent) -> Unit,
    modifier: Modifier = Modifier,
    selectedBookId: String? = null
) {
    // 검색어가 없으면 불러올 목록이 없으므로 안내 문구를 보여 준다
    if (state.query
            .isBlank()
    ) {
        EmptyMessage(message = stringResource(R.string.feature_search_empty_query), modifier = modifier)
        return
    }
    // 처음 불러오다 실패했고 보여 줄 항목이 없으면 목록 자리에 안내와 다시 시도 버튼을 보여 준다.
    // (다음 페이지 실패는 이미 불러온 항목을 두고 목록 끝에 보인다)
    val refreshError: Throwable? = (books.loadState.refresh as? LoadState.Error)?.error
    if (refreshError != null && books.itemCount == 0) {
        ListErrorView(error = refreshError, onRetry = books::retry, modifier = modifier)
        return
    }
    // 처음 불러오는 중이고 보여 줄 항목이 없으면 항목 모양의 자리표시를 보여 준다.
    // (검색어를 바꾸는 중에도 이전 목록이 남아 있으면 그대로 두어 깜빡이지 않게 한다)
    if (books.loadState.refresh is LoadState.Loading && books.itemCount == 0) {
        if (isGrid) {
            GridSkeleton(modifier = modifier)
        } else {
            ListSkeleton(modifier = modifier)
        }
        return
    }
    // 검색을 마쳤는데 결과가 없으면 안내 문구를 보여 준다
    if (books.loadState
            .isNoResult(books.itemCount)
    ) {
        EmptyMessage(message = stringResource(R.string.feature_search_no_result), modifier = modifier)
        return
    }
    if (isGrid) {
        BookGrid(
            state = state,
            books = books,
            onIntent = onIntent,
            selectedBookId = selectedBookId,
            modifier = modifier
        )
    } else {
        BookList(
            state = state,
            books = books,
            onIntent = onIntent,
            selectedBookId = selectedBookId,
            modifier = modifier
        )
    }
}

@Composable
private fun BookList(
    state: SearchUiState,
    books: LazyPagingItems<BookUiModel>,
    onIntent: (SearchIntent) -> Unit,
    selectedBookId: String?,
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
                publishedAt = book.publishedAt,
                price = book.priceText(),
                // 정가와 판매가가 다를 때만 판매가를 따로 보여 준다
                salePrice = book.salePriceText(),
                thumbnailUrl = book.thumbnailUrl,
                isFavorite = book.id in state.favoriteIds,
                selected = book.id == selectedBookId,
                onClick = { onIntent(SearchIntent.OpenDetail(book)) },
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book.id)) }
            )
        }
        // 다음 페이지를 불러오다 실패하면 이미 불러온 항목 아래에 안내와 다시 시도 버튼을 둔다
        (books.loadState.append as? LoadState.Error)?.let { append ->
            item { ListFooterError(error = append.error, onRetry = books::retry) }
        }
    }
}

@Composable
private fun BookGrid(
    state: SearchUiState,
    books: LazyPagingItems<BookUiModel>,
    onIntent: (SearchIntent) -> Unit,
    selectedBookId: String?,
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
                price = book.priceText(),
                salePrice = book.salePriceText(),
                isFavorite = book.id in state.favoriteIds,
                selected = book.id == selectedBookId,
                onClick = { onIntent(SearchIntent.OpenDetail(book)) },
                onFavoriteClick = { onIntent(SearchIntent.ToggleFavorite(book.id)) }
            )
        }
        // 격자에서는 안내가 한 칸이 아니라 한 줄 전체를 차지하게 한다
        (books.loadState.append as? LoadState.Error)?.let { append ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                ListFooterError(error = append.error, onRetry = books::retry)
            }
        }
    }
}

@Composable
private fun ListSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false
    ) {
        items(count = SKELETON_COUNT) { BookListItemSkeleton() }
    }
}

@Composable
private fun GridSkeleton(modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = BookGridColumns,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(count = SKELETON_COUNT) { BookGridItemSkeleton() }
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
