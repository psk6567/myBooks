package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/** 목록에서 하나를 고르는 항목. [BottomSheet] 안의 선택지에 쓴다. 선택된 항목은 브랜드 색으로 표시된다. */
@Composable
fun OptionItem(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val textColor: Color = if (selected) MyBooksTheme.colors.brand else MyBooksTheme.colors.textPrimary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text(text = text, style = MyBooksTheme.typography.normal16, color = textColor)
    }
}

@Preview(name = "OptionItem")
@Composable
private fun OptionItemPreview() {
    MyBooksTheme {
        androidx.compose.foundation.layout.Column {
            OptionItem(text = "제목 오름차순", selected = true, onClick = {})
            OptionItem(text = "제목 내림차순", selected = false, onClick = {})
        }
    }
}
