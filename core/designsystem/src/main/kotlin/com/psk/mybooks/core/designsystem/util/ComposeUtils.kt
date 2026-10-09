package com.psk.mybooks.core.designsystem.util

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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

/** 넓은 화면(태블릿, 펼친 폴더블)으로 보는 창의 최소 폭 */
private val WIDE_SCREEN_MIN_WIDTH: Dp = 640.dp

/**
 * 앱 창의 폭이 640dp 이상이면 true. 태블릿이나 펼친 폴더블처럼 넓은 화면에 맞는 구성을 고를 때 쓴다.
 *
 * ```
 * if (isWideScreen()) { /* 넓은 화면 구성 */ } else { /* 좁은 화면 구성 */ }
 * ```
 *
 * 기기 화면이 아니라 **앱 창**의 폭을 본다. 분할 화면(멀티 윈도우)으로 앱이 좁아지면 false 가 되고,
 * 폴더블을 접거나 펼치면 바뀐 폭에 맞춰 다시 계산된다.
 */
@Composable
fun isWideScreen(): Boolean = LocalWindowInfo.current.containerDpSize.width >= WIDE_SCREEN_MIN_WIDTH
