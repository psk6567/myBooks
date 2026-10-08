package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * 디자인 시스템 아이콘을 그린다. feature 는 Material Icon 대신 이것을 쓴다.
 *
 * @param contentDescription 낭독기 설명. 장식용이거나 옆에 라벨이 있으면 null.
 * @param tint 시맨틱 색(`MyBooksTheme.colors.xxx`)만 넘긴다.
 */
@Composable
fun Icon(
    icon: MbIcon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MyBooksTheme.colors.icon,
    size: Dp = 24.dp
) {
    MaterialIcon(
        painter = painterResource(icon.resId),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size)
    )
}

@Preview
@Composable
private fun IconPreview() {
    MyBooksTheme {
        Icon(icon = MbIcon.Favorite, contentDescription = null, tint = MyBooksTheme.colors.favorite)
    }
}
