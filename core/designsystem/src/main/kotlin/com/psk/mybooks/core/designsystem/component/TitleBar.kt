package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.R
import com.psk.mybooks.core.designsystem.icon.MbIcon
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * 화면 상단 바. 제목은 항상 가운데에 오고, 양옆 영역 너비에 맞춰 겹치지 않게 말줄임된다.
 *
 * @param label 가운데 제목. null 이면 표시하지 않는다.
 * @param onBackClick 뒤로가기 동작. null 이면 뒤로가기 버튼을 표시하지 않는다.
 * @param rightArea 오른쪽 영역. 아이콘 버튼 등을 주입한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitleBar(
    modifier: Modifier = Modifier,
    label: String? = null,
    onBackClick: (() -> Unit)? = null,
    rightArea: @Composable RowScope.() -> Unit = {}
) {
    // ponytail: 가운데 정렬·겹침 방지는 Material3 CenterAlignedTopAppBar 에 맡긴다
    CenterAlignedTopAppBar(
        title = {
            if (label != null) {
                Text(
                    text = label,
                    style = MyBooksTheme.typography.normal16,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (onBackClick != null) BackButton(onClick = onBackClick)
        },
        actions = rightArea,
        expandedHeight = 56.dp,
        // 상태바 여백은 화면(Scaffold)에서 처리한다
        windowInsets = WindowInsets(0),
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MyBooksTheme.colors.background,
            titleContentColor = MyBooksTheme.colors.textPrimary,
            navigationIconContentColor = MyBooksTheme.colors.icon,
            actionIconContentColor = MyBooksTheme.colors.icon
        )
    )
}

@Composable
private fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon = MbIcon.ArrowBack,
            contentDescription = stringResource(R.string.core_designsystem_back)
        )
    }
}

@Preview(name = "Label")
@Composable
private fun TitleBarLabelPreview() {
    MyBooksTheme {
        TitleBar(label = "검색")
    }
}

@Preview(name = "Back + RightArea")
@Composable
private fun TitleBarBackPreview() {
    MyBooksTheme {
        TitleBar(
            onBackClick = {},
            rightArea = { Text("오른쪽", modifier = Modifier.padding(horizontal = 12.dp)) }
        )
    }
}

@Preview(name = "Back + Label + RightArea (Dark)")
@Composable
private fun TitleBarDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        TitleBar(
            label = "아주 긴 제목이 들어가면 말줄임 처리가 됩니다 아주 긴 제목",
            onBackClick = {},
            rightArea = { Text("오른쪽", modifier = Modifier.padding(horizontal = 12.dp)) }
        )
    }
}
