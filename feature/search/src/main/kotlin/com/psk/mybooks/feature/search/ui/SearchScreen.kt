package com.psk.mybooks.feature.search.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/** ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다. */
@Composable
fun SearchRoute(
    modifier: Modifier = Modifier,
) {
    val viewModel: SearchViewModel = viewModel()
    SearchScreen(modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}
