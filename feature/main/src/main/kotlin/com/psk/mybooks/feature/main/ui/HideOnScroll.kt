package com.psk.mybooks.feature.main.ui

import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

/**
 * 목록을 위로 스크롤하면 [hideOnScroll] 을 단 영역이 같이 올라가며 사라지고, 아래로 스크롤하면 다시 내려온다.
 * 자식 목록의 스크롤을 [connection] 으로 받기만 하고 소비하지 않으므로, 목록은 평소처럼 스크롤된다.
 */
@Stable
internal class HideOnScrollState {
    /** 영역 높이(px). 레이아웃에서 측정되어 채워진다. */
    var heightPx: Float = 0f
        internal set

    /** 0(전부 보임) ~ -[heightPx](전부 숨김) */
    var offsetPx: Float by mutableFloatStateOf(0f)
        private set

    val connection: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            offsetPx = (offsetPx + available.y).coerceIn(-heightPx, 0f)
            return Offset.Zero
        }

        // 손을 떼면 절반 넘게 숨겨졌는지에 따라 끝까지 숨기거나 보여서, 반쯤 걸친 채로 멈추지 않게 한다
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            animateTo(if (offsetPx < -heightPx / 2) -heightPx else 0f)
            return Velocity.Zero
        }
    }

    /** 숨겨져 있더라도 다시 보이게 한다. (목록이 짧아져 더는 스크롤할 수 없을 때 등) */
    suspend fun show() {
        animateTo(0f)
    }

    private suspend fun animateTo(target: Float) {
        animate(initialValue = offsetPx, targetValue = target) { value, _ -> offsetPx = value }
    }
}

/** [state] 의 offset 만큼 위로 밀려 올라가고, 줄어든 높이만큼 아래 영역이 자리를 넘겨받는다. */
internal fun Modifier.hideOnScroll(state: HideOnScrollState): Modifier = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        state.heightPx = placeable.height.toFloat()
        val visibleHeight: Int = (placeable.height + state.offsetPx).roundToInt().coerceAtLeast(0)
        layout(placeable.width, visibleHeight) {
            placeable.place(0, state.offsetPx.roundToInt())
        }
    }
