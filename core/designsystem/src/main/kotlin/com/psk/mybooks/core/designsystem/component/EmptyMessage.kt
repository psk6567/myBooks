package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/** 목록이 비었을 때 가운데에 안내 문구를 보여 주는 화면. 빈 영역 전체를 채운다. */
@Composable
fun EmptyMessage(message: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MyBooksTheme.typography.normal16,
            color = MyBooksTheme.colors.textTertiary
        )
    }
}

@Preview(name = "Empty", widthDp = 360, heightDp = 400)
@Composable
private fun EmptyMessagePreview() {
    MyBooksTheme {
        EmptyMessage(message = "안내 문구")
    }
}
