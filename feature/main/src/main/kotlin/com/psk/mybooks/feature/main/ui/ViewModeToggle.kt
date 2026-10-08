package com.psk.mybooks.feature.main.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.component.Icon
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.model.ViewMode
import com.psk.mybooks.feature.main.R

private fun ViewMode.icon(): MbIcon = when (this) {
    ViewMode.Grid -> MbIcon.ViewGrid
    ViewMode.List -> MbIcon.ViewList
}

@StringRes
private fun ViewMode.labelRes(): Int = when (this) {
    ViewMode.Grid -> R.string.feature_main_view_mode_grid
    ViewMode.List -> R.string.feature_main_view_mode_list
}

/** 격자/목록 보기 전환. 항목 사이에 구분선이 있고, 선택된 쪽만 진하게 보인다. */
@Composable
internal fun ViewModeToggle(selected: ViewMode, onSelect: (ViewMode) -> Unit, modifier: Modifier = Modifier) {
    val colors: MbColors = MyBooksTheme.colors

    Row(
        // 아이콘 양옆 12dp 도 눌리도록 padding 을 항목 안에 두었으므로, 오른쪽 끝의 padding 만큼 밀어 아이콘을 가장자리에 맞춘다
        modifier = modifier.height(32.dp).offset(x = 12.dp).selectableGroup(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ViewMode.entries.forEachIndexed { index, mode ->
            if (index > 0) {
                VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp, color = colors.divider)
            }
            val isSelected: Boolean = mode == selected
            val tint: Color = if (isSelected) colors.icon else colors.iconInactive
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .selectable(
                        selected = isSelected,
                        interactionSource = null,
                        indication = null,
                        role = Role.RadioButton,
                        onClick = { onSelect(mode) }
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon = mode.icon(), contentDescription = stringResource(mode.labelRes()), tint = tint)
            }
        }
    }
}

@Preview(name = "List selected", widthDp = 120)
@Composable
private fun ViewModeTogglePreview() {
    MyBooksTheme {
        ViewModeToggle(selected = ViewMode.List, onSelect = {})
    }
}
