package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer

/**
 * 도서 그리드 아이템 (표지 + 제목 + 출판사). 한 줄에 3개씩 놓는 용도.
 * 가로 폭은 그리드(호출하는 쪽)가 정하고, 표지는 폭에 맞춰 2:3 비율로 그려진다.
 *
 * @param thumbnailUrl 표지 이미지 URL. 불러오는 중·실패·빈 값 표시는 [NetworkImage] 가 맡는다.
 */
@Composable
fun BookGridItem(
    title: String,
    publisher: String,
    thumbnailUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        // 제목이 함께 읽히므로 표지 설명은 생략한다
        NetworkImage(
            url = thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
        )
        VSpacer(8.dp)
        Text(
            text = title,
            style = typography.normal14,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
        Text(
            text = publisher,
            style = typography.normal12,
            color = colors.textTertiary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

@Preview(name = "3 per row", widthDp = 360)
@Composable
private fun GridItemRowPreview() {
    MyBooksTheme {
        Row(
            modifier = Modifier
                .background(MyBooksTheme.colors.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BookGridItem(title = "도서 제목", publisher = "출판사", thumbnailUrl = "", onClick = {
            }, modifier = Modifier.weight(1f))
            BookGridItem(
                title = "아주 긴 도서 제목이 들어가면 한 줄로 말줄임 처리가 됩니다",
                publisher = "아주 긴 출판사 이름",
                thumbnailUrl = "",
                onClick = {},
                modifier = Modifier.weight(1f)
            )
            BookGridItem(title = "세 번째", publisher = "출판사", thumbnailUrl = "", onClick = {
            }, modifier = Modifier.weight(1f))
        }
    }
}

@Preview(name = "Dark", widthDp = 360)
@Composable
private fun GridItemDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        Row(
            modifier = Modifier
                .background(MyBooksTheme.colors.background)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BookGridItem(title = "도서 제목", publisher = "출판사", thumbnailUrl = "", onClick = {
            }, modifier = Modifier.weight(1f))
            BookGridItem(title = "도서 제목", publisher = "출판사", thumbnailUrl = "", onClick = {
            }, modifier = Modifier.weight(1f))
            BookGridItem(title = "도서 제목", publisher = "출판사", thumbnailUrl = "", onClick = {
            }, modifier = Modifier.weight(1f))
        }
    }
}
