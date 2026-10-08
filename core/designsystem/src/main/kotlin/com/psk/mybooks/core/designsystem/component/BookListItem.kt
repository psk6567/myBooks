package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.R
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer
import com.psk.mybooks.core.designsystem.util.VSpacer

/**
 * 도서 리스트 아이템 (썸네일 + 정보 + 즐겨찾기).
 * 도메인 모델을 모르므로 화면에 보일 값을 문자열로 받는다. 가격 포맷 등은 호출하는 쪽에서 만든다.
 *
 * @param status 제목 위의 판매 상태 (예: 정상판매, 품절)
 * @param publisher 출판사. "출판사 : " 라벨은 이 컴포넌트가 붙인다.
 * @param authors 저자. "저자 : " 라벨은 이 컴포넌트가 붙인다.
 * @param price 오른쪽 아래 가격 (포맷된 문자열)
 * @param thumbnail 썸네일 영역. 이미지 로딩(Coil)은 호출하는 쪽에서 주입한다.
 */
@Composable
fun BookListItem(
    status: String,
    title: String,
    publisher: String,
    authors: String,
    price: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnail: @Composable () -> Unit = {}
) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .size(width = 72.dp, height = 108.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
            ) {
                thumbnail()
            }
            HSpacer(12.dp)
            // 썸네일 높이를 최소로, 내용이 길면 그만큼 늘어난다. 가격은 항상 내용 아래에 와서 겹치지 않는다.
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 108.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 오른쪽 위 즐겨찾기 버튼(32dp)과 겹치지 않게 비운다
                Column(modifier = Modifier.padding(top = 4.dp, end = 40.dp)) {
                    Text(text = status, style = typography.normal10, color = colors.textTertiary)
                    VSpacer(4.dp)
                    Text(
                        text = title,
                        style = typography.normal16,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    VSpacer(4.dp)
                    Text(
                        text = stringResource(R.string.core_designsystem_list_item_publisher, publisher),
                        style = typography.normal12,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    VSpacer(4.dp)
                    Text(
                        text = stringResource(R.string.core_designsystem_list_item_authors, authors),
                        style = typography.normal12,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = price,
                    style = typography.normal16,
                    color = colors.textPrimary,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp, end = 4.dp, bottom = 4.dp)
                )
            }
        }
        FavoriteButton(
            isFavorite = isFavorite,
            onClick = onFavoriteClick,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors: MbColors = MyBooksTheme.colors
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon = MbIcon.Favorite,
            contentDescription = stringResource(
                if (isFavorite) R.string.core_designsystem_favorite_remove else R.string.core_designsystem_favorite_add
            ),
            tint = if (isFavorite) colors.favorite else colors.iconInactive,
            size = 18.dp
        )
    }
}

@Preview(name = "Favorite", widthDp = 360)
@Composable
private fun ListItemFavoritePreview() {
    MyBooksTheme {
        BookListItem(
            status = "정상판매",
            title = "도서 제목",
            publisher = "출판사",
            authors = "저자",
            price = "18,000원",
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(name = "Long text (Dark)", widthDp = 360)
@Composable
private fun ListItemLongTextDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        BookListItem(
            status = "품절",
            title = "아주 긴 도서 제목이 들어가면 두 줄까지 보이고 나머지는 말줄임 처리가 됩니다",
            publisher = "아주 긴 출판사 이름이 들어가는 경우",
            authors = "저자 A, 저자 B, 저자 C",
            price = "1,234,000원",
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}
