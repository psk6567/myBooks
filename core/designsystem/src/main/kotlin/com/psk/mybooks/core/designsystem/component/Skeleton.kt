package com.psk.mybooks.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer
import com.psk.mybooks.core.designsystem.util.VSpacer

private const val SKELETON_MIN_ALPHA: Float = 0.45f
private const val SKELETON_PULSE_MILLIS: Int = 800

/** 불러오는 동안 자리를 잡아 두는 회색 막대. 천천히 깜빡여서 멈춰 있지 않다는 것을 보여 준다. */
@Composable
internal fun SkeletonBox(modifier: Modifier = Modifier) {
    val colors: MbColors = MyBooksTheme.colors
    val alpha: State<Float> = rememberInfiniteTransition(label = "skeleton")
        .animateFloat(
            initialValue = 1f,
            targetValue = SKELETON_MIN_ALPHA,
            animationSpec = infiniteRepeatable(tween(SKELETON_PULSE_MILLIS), RepeatMode.Reverse),
            label = "skeletonAlpha"
        )
    Box(
        modifier = modifier
            .alpha(alpha.value)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surfaceVariant)
    )
}

/** [BookListItem] 과 같은 크기의 불러오는 중 자리. 모양이 바뀌어도 목록이 튀지 않게 크기를 맞춘다. */
@Composable
fun BookListItemSkeleton(modifier: Modifier = Modifier) {
    val colors: MbColors = MyBooksTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.card)
            .padding(8.dp)
    ) {
        SkeletonBox(modifier = Modifier.size(width = 72.dp, height = 108.dp))
        HSpacer(12.dp)
        Column(modifier = Modifier.padding(top = 4.dp)) {
            SkeletonBox(modifier = Modifier.size(width = 48.dp, height = 10.dp))
            VSpacer(8.dp)
            SkeletonBox(modifier = Modifier.size(width = 160.dp, height = 16.dp))
            VSpacer(8.dp)
            SkeletonBox(modifier = Modifier.size(width = 110.dp, height = 12.dp))
            VSpacer(6.dp)
            SkeletonBox(modifier = Modifier.size(width = 80.dp, height = 12.dp))
        }
    }
}

/** [BookGridItem] 과 같은 크기의 불러오는 중 자리. 표지(2:3)와 아래 가격 줄이다. */
@Composable
fun BookGridItemSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
        )
        VSpacer(10.dp)
        SkeletonBox(
            modifier = Modifier
                .width(64.dp)
                .height(14.dp)
        )
    }
}

@Preview(name = "BookListItemSkeleton", widthDp = 360)
@Composable
private fun BookListItemSkeletonPreview() {
    MyBooksTheme {
        BookListItemSkeleton(modifier = Modifier.padding(16.dp))
    }
}

@Preview(name = "BookGridItemSkeleton", widthDp = 120)
@Composable
private fun BookGridItemSkeletonPreview() {
    MyBooksTheme {
        BookGridItemSkeleton(modifier = Modifier.padding(8.dp))
    }
}
