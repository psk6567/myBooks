package com.psk.mybooks.feature.detail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.psk.mybooks.core.designsystem.component.FavoriteButton
import com.psk.mybooks.core.designsystem.component.MbSnackbarHost
import com.psk.mybooks.core.designsystem.component.NetworkImage
import com.psk.mybooks.core.designsystem.component.SnackbarProvider
import com.psk.mybooks.core.designsystem.component.TitleBar
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer
import com.psk.mybooks.core.ui.BookUiModel
import com.psk.mybooks.core.ui.ErrorEffect
import com.psk.mybooks.core.ui.priceText
import com.psk.mybooks.core.ui.salePriceText
import com.psk.mybooks.feature.detail.R

/** 표지 이미지의 폭. 높이는 2:3 비율로 정해진다. */
private val THUMBNAIL_WIDTH: Dp = 180.dp

/** 라벨 칸의 폭 */
private val LABEL_WIDTH: Dp = 88.dp

/**
 * ViewModel 을 연결하는 진입점. 이 화면은 자기 Scaffold 를 가지므로 스낵바 호스트도 여기서 둔다.
 * (홈의 호스트는 홈 화면 안에만 있어서, 상세에서 난 오류는 이 화면의 호스트로 보여 준다)
 *
 * @param book 보여 줄 도서
 * @param onBack 뒤로가기를 눌렀을 때. 화면 이동은 main 이 연결한다. null 이면 뒤로가기 버튼이 없는
 * 목록 옆 패널(넓은 화면)로 그려진다.
 */
@Composable
internal fun DetailRoute(book: BookUiModel, onBack: (() -> Unit)?, modifier: Modifier = Modifier) {
    // 도서마다 ViewModel 을 따로 만든다. 패널에서 다른 도서를 고르면 그 도서의 상태로 바뀐다.
    val viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        key = book.id,
        creationCallback = { factory -> factory.create(book) }
    )
    val state: DetailUiState by viewModel.state
        .collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
    val currentOnBack: (() -> Unit)? by rememberUpdatedState(onBack)

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DetailEffect.Close -> currentOnBack?.invoke()
            }
        }
    }

    SnackbarProvider(snackbarHostState) {
        // 오류를 중요도에 따라 스낵바나 다이얼로그로 알린다
        ErrorEffect(viewModel.error)
        DetailScreen(
            state = state,
            onIntent = viewModel::onIntent,
            snackbarHostState = snackbarHostState,
            isEmbedded = onBack == null,
            modifier = modifier
        )
    }
}

/**
 * 위에 표지, 아래에 도서 정보를 라벨과 값으로 나열한다. 값이 없는 항목은 보이지 않는다.
 * 상단 바 오른쪽에 즐겨찾기 버튼이 있다.
 *
 * @param isEmbedded 목록 옆 패널로 그려지는지. 그러면 뒤로가기 버튼이 없고, 이미 홈의 상단 바와 시스템 바가 위를 차지하므로
 * 상태 표시줄과 시스템 바 여백을 다시 두지 않는다.
 */
@Composable
internal fun DetailScreen(
    state: DetailUiState,
    onIntent: (DetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false
) {
    val book: BookUiModel = state.book
    val rows: List<Pair<String, String>> = detailRows(book)

    Scaffold(
        modifier = modifier,
        containerColor = MyBooksTheme.colors.background,
        contentWindowInsets = if (isEmbedded) WindowInsets(0) else ScaffoldDefaults.contentWindowInsets,
        snackbarHost = { MbSnackbarHost(snackbarHostState) },
        topBar = {
            TitleBar(
                label = stringResource(R.string.feature_detail_title),
                onBackClick = if (isEmbedded) null else ({ onIntent(DetailIntent.Back) }),
                windowInsets = if (isEmbedded) WindowInsets(0) else WindowInsets.statusBars,
                rightArea = {
                    FavoriteButton(
                        isFavorite = state.isFavorite,
                        onClick = { onIntent(DetailIntent.ToggleFavorite) },
                        size = 40.dp
                    )
                    HSpacer(8.dp)
                }
            )
        }
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목이 아래 목록에 있으므로 표지 설명은 생략한다
            NetworkImage(
                url = book.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .width(THUMBNAIL_WIDTH)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                rows.forEachIndexed { index, (label, value) ->
                    if (index > 0) HorizontalDivider(color = MyBooksTheme.colors.divider)
                    InfoRow(label = label, value = value)
                }
            }
        }
    }
}

/** 화면에 보일 (라벨, 값) 목록. 값이 비어 있는 항목은 뺀다. 저자·번역자는 이미 쉼표로 이어 붙은 한 줄이다. */
@Composable
private fun detailRows(book: BookUiModel): List<Pair<String, String>> {
    val all: List<Pair<String, String?>> = listOf(
        stringResource(R.string.feature_detail_label_title) to book.title,
        stringResource(R.string.feature_detail_label_authors) to book.authors,
        stringResource(R.string.feature_detail_label_translators) to book.translators,
        stringResource(R.string.feature_detail_label_publisher) to book.publisher,
        stringResource(R.string.feature_detail_label_published_at) to book.publishedAt,
        stringResource(R.string.feature_detail_label_isbn) to book.isbn,
        stringResource(R.string.feature_detail_label_price) to book.priceText(),
        stringResource(R.string.feature_detail_label_sale_price) to book.salePriceText(),
        stringResource(R.string.feature_detail_label_status) to book.status,
        stringResource(R.string.feature_detail_label_contents) to book.contents,
        stringResource(R.string.feature_detail_label_url) to book.url
    )
    return all.mapNotNull { (label, value) -> if (value.isNullOrBlank()) null else label to value }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = typography.normal14,
            color = colors.textTertiary,
            modifier = Modifier.width(LABEL_WIDTH)
        )
        Text(text = value, style = typography.normal14, color = colors.textPrimary, modifier = Modifier.weight(1f))
    }
}

/**
 * 넓은 화면에서 목록 옆에 보이는 상세. 뒤로가기 버튼이 없다. 화면 이동 없이 [book] 만 바꿔서 다른 도서를 보여 준다.
 */
@Composable
fun DetailPane(book: BookUiModel, modifier: Modifier = Modifier) {
    DetailRoute(book = book, onBack = null, modifier = modifier)
}

private val previewBook: BookUiModel = BookUiModel(
    id = "1",
    status = "정상판매",
    title = "안드로이드 with Kotlin 앱 프로그래밍 가이드",
    contents = "자바와 코틀린으로 직접 구현하며 익히는 실전형 앱 프로그래밍 완벽 가이드!",
    url = "https://search.daum.net/search?w=bookpage&bookId=4851688",
    isbn = "1186886897 9791186886892",
    publishedAt = "2019-01-12",
    publisher = "아이콕스",
    authors = "안귀정, 김저자",
    translators = "",
    price = 40000,
    salePrice = 36000,
    thumbnailUrl = ""
)

@Preview(name = "Detail", widthDp = 360, heightDp = 800)
@Composable
private fun DetailScreenPreview() {
    MyBooksTheme {
        DetailScreen(
            state = DetailUiState(book = previewBook, isFavorite = true),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Detail (Dark)", widthDp = 360, heightDp = 800)
@Composable
private fun DetailScreenDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        DetailScreen(
            state = DetailUiState(book = previewBook.copy(salePrice = null, translators = "번역자")),
            onIntent = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
