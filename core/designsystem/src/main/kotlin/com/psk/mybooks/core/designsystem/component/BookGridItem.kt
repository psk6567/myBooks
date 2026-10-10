package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.selectedOutline

/** 하단 띠(제목, 출판사)의 안쪽 여백 */
private val INFO_PADDING: Dp = 6.dp

/** 표지 뒤쪽을 흐리게 만드는 정도 */
private val INFO_BLUR_RADIUS: Dp = 14.dp

/** 가격 영역의 높이. 할인 여부와 상관없이 같아서 한 줄의 항목 높이가 들쭉날쭉하지 않다. */
private val PRICE_AREA_HEIGHT: Dp = 40.dp

/**
 * 도서 그리드 아이템. 표지 아래쪽에 흐리게 처리한 띠를 두고 제목과 출판사를 얹고, 표지 아래에는 가격을 보여 준다.
 * 표지 오른쪽 위에 즐겨찾기 버튼이 있다. 가로 폭은 그리드(호출하는 쪽)가 정하고, 표지는 폭에 맞춰 2:3 비율로 그려진다.
 *
 * 띠는 같은 표지를 흐리게 겹쳐 그린다. 흐림은 Android 12(API 31) 이상에서만 적용되고, 그보다 낮으면 흐림 없이
 * 어두운 막만 깔려 글자는 그대로 읽힌다.
 *
 * @param thumbnailUrl 표지 이미지 URL. 불러오는 중·실패·빈 값 표시는 [NetworkImage] 가 맡는다.
 * @param price 정가 (포맷된 문자열). [salePrice] 가 있으면 취소선으로 작게 보인다.
 * @param salePrice 정가와 다른 판매가 (포맷된 문자열). 있으면 빨간 글씨로 크게 보이고, 없으면 [price] 만 보인다.
 * @param selected 지금 고른 항목이면 표지에 테두리를 그려 강조한다. (넓은 화면에서 옆 패널에 상세가 열린 항목)
 */
@Composable
fun BookGridItem(
    title: String,
    publisher: String,
    thumbnailUrl: String,
    price: String,
    salePrice: String?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .selectedOutline(selected, RoundedCornerShape(8.dp))
        ) {
            // 표지 전체의 크기. 아래 띠 안의 복사본이 이 크기여야 표지의 아래쪽과 이어진다
            val coverWidth: Dp = maxWidth
            val coverHeight: Dp = maxHeight
            // 제목이 함께 읽히므로 표지 설명은 생략한다
            NetworkImage(url = thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
            // 아래쪽 띠: 표지 전체 크기의 흐린 복사본을 아래에 맞춰 깔고 글자 높이만큼만 보이게 자른다
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                // matchParentSize 안에 넣어 복사본의 크기가 띠의 높이에 영향을 주지 않게 한다(높이는 글자만으로 정해진다)
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clipToBounds()
                ) {
                    NetworkImage(
                        url = thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .requiredSize(width = coverWidth, height = coverHeight)
                            .align(Alignment.BottomCenter)
                            .blur(radius = INFO_BLUR_RADIUS, edgeTreatment = BlurredEdgeTreatment.Rectangle)
                    )
                }
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(colors.imageOverlay)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(INFO_PADDING)
                ) {
                    Text(
                        text = title,
                        style = typography.bold12,
                        color = colors.onImageOverlay,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = publisher,
                        style = typography.normal10,
                        color = colors.onImageOverlay
                            .copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            FavoriteButton(
                isFavorite = isFavorite,
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp),
                size = 28.dp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(PRICE_AREA_HEIGHT)
                .padding(horizontal = 2.dp, vertical = 6.dp)
        ) {
            if (salePrice == null) {
                Text(text = price, style = typography.bold14, color = colors.textPrimary, maxLines = 1)
            } else {
                Text(text = salePrice, style = typography.bold14, color = colors.salePrice, maxLines = 1)
                Text(
                    text = price,
                    style = typography.normal10
                        .copy(textDecoration = TextDecoration.LineThrough),
                    color = colors.textTertiary,
                    maxLines = 1
                )
            }
        }
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
            BookGridItem(
                title = "도서 제목",
                publisher = "출판사",
                thumbnailUrl = "",
                price = "20,000원",
                salePrice = "18,000원",
                isFavorite = false,
                onClick = {},
                onFavoriteClick = {},
                modifier = Modifier.weight(1f)
            )
            BookGridItem(
                title = "아주 긴 도서 제목이 들어가면 두 줄까지만 보이고 말줄임 처리가 됩니다",
                publisher = "아주 긴 출판사 이름",
                thumbnailUrl = "",
                price = "1,234,000원",
                salePrice = null,
                isFavorite = true,
                onClick = {},
                onFavoriteClick = {},
                modifier = Modifier.weight(1f)
            )
            BookGridItem(
                title = "세 번째",
                publisher = "출판사",
                thumbnailUrl = "",
                price = "9,000원",
                salePrice = null,
                isFavorite = false,
                onClick = {},
                onFavoriteClick = {},
                modifier = Modifier.weight(1f)
            )
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
            BookGridItem(
                title = "도서 제목",
                publisher = "출판사",
                thumbnailUrl = "",
                price = "20,000원",
                salePrice = "18,000원",
                isFavorite = false,
                onClick = {},
                onFavoriteClick = {},
                modifier = Modifier.weight(1f)
            )
            BookGridItem(
                title = "도서 제목",
                publisher = "출판사",
                thumbnailUrl = "",
                price = "15,000원",
                salePrice = null,
                isFavorite = true,
                onClick = {},
                onFavoriteClick = {},
                modifier = Modifier.weight(1f)
            )
        }
    }
}
