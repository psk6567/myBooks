package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer

/**
 * 금액처럼 숫자만 입력받는 입력창. 숫자가 아닌 글자는 입력되지 않고, 최대 [maxLength] 자리까지 받는다.
 * 비어 있으면 [placeholder] 가 보이고, 오른쪽에 단위([suffix])가 붙는다.
 *
 * @param value 입력된 숫자 문자열. 비어 있으면 값이 없는 것이다.
 */
@Composable
fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    suffix: String,
    modifier: Modifier = Modifier,
    maxLength: Int = 9
) {
    val colors: MbColors = MyBooksTheme.colors
    val textStyle: TextStyle = MyBooksTheme.typography.normal14.copy(
        color = colors.textPrimary,
        textAlign = TextAlign.End
    )

    BasicTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter(Char::isDigit).take(maxLength)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.brand),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, style = textStyle, color = colors.textTertiary, maxLines = 1)
                    }
                    innerTextField()
                }
                HSpacer(4.dp)
                Text(text = suffix, style = MyBooksTheme.typography.normal14, color = colors.textSecondary)
            }
        }
    )
}

@Preview(name = "Empty")
@Composable
private fun AmountFieldEmptyPreview() {
    MyBooksTheme {
        AmountField(value = "", onValueChange = {}, placeholder = "최소 금액", suffix = "원")
    }
}

@Preview(name = "Filled (Dark)")
@Composable
private fun AmountFieldDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        AmountField(value = "15000", onValueChange = {}, placeholder = "최소 금액", suffix = "원")
    }
}
