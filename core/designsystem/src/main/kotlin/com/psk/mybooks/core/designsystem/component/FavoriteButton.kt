package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.R
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * 동그란 즐겨찾기(하트) 버튼. 도서 아이템 위나 상세 화면의 상단 바 오른쪽에 쓴다. 아이콘은 버튼 크기의 9/16 이다.
 *
 * @param isFavorite 즐겨찾기 여부. 켜져 있으면 하트가 채워지고 낭독 설명이 "해제"로 바뀐다.
 */
@Composable
fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier, size: Dp = 32.dp) {
    val colors: MbColors = MyBooksTheme.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.surfaceVariant)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon = MbIcon.Favorite,
            contentDescription = stringResource(
                if (isFavorite) R.string.core_designsystem_favorite_remove else R.string.core_designsystem_favorite_add
            ),
            tint = if (isFavorite) colors.favorite else colors.iconInactive,
            size = size * 9 / 16
        )
    }
}
