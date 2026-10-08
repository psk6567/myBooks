package com.psk.mybooks.core.designsystem.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** 세로 여백. `Spacer(Modifier.height(dp))` 의 숏컷 */
@Composable
fun VSpacer(height: Dp) {
    Spacer(Modifier.height(height))
}

/** 가로 여백. `Spacer(Modifier.width(dp))` 의 숏컷 */
@Composable
fun HSpacer(width: Dp) {
    Spacer(Modifier.width(width))
}
