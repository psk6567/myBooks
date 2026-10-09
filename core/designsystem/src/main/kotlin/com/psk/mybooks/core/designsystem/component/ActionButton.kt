package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * 화면 안의 동작 버튼. [primary] 면 브랜드 색으로 채워지고(적용, 확인), 아니면 테두리만 있다(초기화, 취소).
 * [enabled] 가 false 면 눌리지 않고 흐리게 보인다.
 */
@Composable
fun ActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true
) {
    val colors: MbColors = MyBooksTheme.colors
    val shape: RoundedCornerShape = RoundedCornerShape(12.dp)
    val background: Color = when {
        !enabled -> colors.surfaceVariant
        primary -> colors.brand
        else -> Color.Transparent
    }
    val textColor: Color = when {
        !enabled -> colors.textTertiary
        primary -> colors.textOnBrand
        else -> colors.textPrimary
    }
    val border: BorderStroke? = if (primary || !enabled) null else BorderStroke(1.dp, colors.border)

    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(background)
            .let { base -> if (border != null) base.border(border, shape) else base }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MyBooksTheme.typography.bold14, color = textColor, maxLines = 1)
    }
}

@Preview(name = "ActionButton")
@Composable
private fun ActionButtonPreview() {
    MyBooksTheme {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            ActionButton(text = "적용", onClick = {})
            ActionButton(text = "초기화", onClick = {}, primary = false)
            ActionButton(text = "적용", onClick = {}, enabled = false)
        }
    }
}
