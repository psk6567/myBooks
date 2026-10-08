package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.R
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer

/**
 * 검색 입력창. 입력이 있으면 오른쪽에 지우기(X) 버튼이 나타난다.
 *
 * @param onSearch 키보드의 검색 버튼을 눌렀을 때. 키보드는 자동으로 내려간다.
 */
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: (String) -> Unit = {}
) {
    val colors = MyBooksTheme.colors
    val textStyle = MyBooksTheme.typography.normal14.copy(color = colors.textPrimary)
    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.brand),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch(query)
            }
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.surfaceVariant)
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = colors.icon,
                    modifier = Modifier.size(20.dp)
                )
                HSpacer(8.dp)
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = colors.textTertiary, maxLines = 1)
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    ClearButton(onClick = { onQueryChange("") })
                } else {
                    HSpacer(16.dp)
                }
            }
        }
    )
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_close),
            contentDescription = stringResource(R.string.core_designsystem_clear_query),
            tint = MyBooksTheme.colors.iconInactive,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(name = "Empty")
@Composable
private fun SearchBarEmptyPreview() {
    MyBooksTheme {
        SearchBar(query = "", onQueryChange = {}, placeholder = "제목 또는 저자를 입력하세요.")
    }
}

@Preview(name = "Filled")
@Composable
private fun SearchBarFilledPreview() {
    MyBooksTheme {
        SearchBar(query = "해리 포터", onQueryChange = {}, placeholder = "제목 또는 저자를 입력하세요.")
    }
}

@Preview(name = "Filled (Dark)")
@Composable
private fun SearchBarDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        SearchBar(query = "해리 포터", onQueryChange = {}, placeholder = "제목 또는 저자를 입력하세요.")
    }
}
