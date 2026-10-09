package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * 테두리만 있는 둥근 텍스트 버튼. 정렬, 필터 같은 옵션 선택 버튼에 쓴다.
 *
 * @param selected 필터가 걸려 있는 것처럼 값이 켜져 있으면 브랜드 색으로 강조한다.
 */
@Composable
fun OutlinedChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false) {
    val shape: RoundedCornerShape = RoundedCornerShape(16.dp)
    val height: Dp = 32.dp
    val contentColor: Color = if (selected) MyBooksTheme.colors.brand else MyBooksTheme.colors.textPrimary
    val borderColor: Color = if (selected) MyBooksTheme.colors.brand else MyBooksTheme.colors.border

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MyBooksTheme.typography.normal12,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(name = "OutlinedChip")
@Composable
private fun OutlinedChipPreview() {
    MyBooksTheme {
        OutlinedChip(text = "제목 오름차순", onClick = {})
    }
}

@Preview(name = "OutlinedChip (Selected)")
@Composable
private fun OutlinedChipSelectedPreview() {
    MyBooksTheme {
        OutlinedChip(text = "10,000~20,000원", onClick = {}, selected = true)
    }
}

@Preview(name = "OutlinedChip (Dark)")
@Composable
private fun OutlinedChipDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        OutlinedChip(text = "제목 오름차순", onClick = {})
    }
}
