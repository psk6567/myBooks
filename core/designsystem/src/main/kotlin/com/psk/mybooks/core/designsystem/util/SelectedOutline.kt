package com.psk.mybooks.core.designsystem.util

import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/** [selected] 이면 브랜드 색 테두리를 그려 지금 고른 항목임을 보여 준다. 아니면 아무것도 바꾸지 않는다. */
@Composable
fun Modifier.selectedOutline(selected: Boolean, shape: Shape): Modifier = if (selected) {
    border(width = 2.dp, color = MyBooksTheme.colors.brand, shape = shape)
} else {
    this
}
