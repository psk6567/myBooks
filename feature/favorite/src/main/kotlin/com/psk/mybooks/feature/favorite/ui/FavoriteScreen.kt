package com.psk.mybooks.feature.favorite.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/** ViewModel 을 연결하는 진입점. Navigation 도입 후에는 internal 로 바꾼다. */
@Composable
fun FavoriteRoute(
    modifier: Modifier = Modifier,
) {
    val viewModel: FavoriteViewModel = viewModel()
    FavoriteScreen(modifier = modifier)
}

/** 상태와 콜백을 파라미터로만 받는 UI. Preview·UI 테스트 대상. */
@Composable
internal fun FavoriteScreen(
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize())
}
