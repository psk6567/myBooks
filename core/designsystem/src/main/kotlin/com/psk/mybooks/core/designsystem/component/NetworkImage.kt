package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * URL 이미지. 이미지 로딩 라이브러리(Coil)는 이 파일에서만 쓴다. 바꿀 때는 이 파일만 고치면 된다.
 *
 * 크기는 [modifier] 로 정하고, 이미지는 그 크기에 맞춰 디코딩된다.
 *
 * - 불러오는 중: `surfaceVariant` 배경만 보인다. (placeholder)
 * - 불러오기 실패, URL 이 비어 있음: 배경 가운데에 깨진 이미지 아이콘을 보인다.
 * - 성공: 서서히 나타난다. (crossfade, 앱의 ImageLoader 설정)
 */
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var isFailed: Boolean by remember(url) { mutableStateOf(false) }

    Box(
        modifier = modifier.background(MyBooksTheme.colors.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onState = { state -> isFailed = state is AsyncImagePainter.State.Error }
        )
        if (isFailed) {
            Icon(
                icon = MbIcon.ImageBroken,
                contentDescription = null,
                tint = MyBooksTheme.colors.iconInactive,
                size = 24.dp
            )
        }
    }
}

@Preview(name = "Loading or failed")
@Composable
private fun NetworkImagePreview() {
    MyBooksTheme {
        NetworkImage(url = "", contentDescription = null, modifier = Modifier.size(72.dp, 108.dp))
    }
}
