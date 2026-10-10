package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.model.PriceFilter
import kotlin.math.roundToInt

/** 슬라이더로 고를 수 있는 가장 높은 금액(원). 이 값에 닿으면 상한이 없다. */
internal const val PRICE_SLIDER_MAX: Float = 100_000f

/** 슬라이더가 움직이는 금액 단위(원) */
private const val PRICE_SLIDER_UNIT: Int = 1_000

/** 슬라이더의 눈금 수. 양 끝을 뺀 칸 수라서 (최대 / 단위) - 1 이다. */
internal const val PRICE_SLIDER_STEPS: Int = (PRICE_SLIDER_MAX.toInt() / PRICE_SLIDER_UNIT) - 1

/**
 * 필터를 슬라이더의 선택 범위로 바꾼다. 제한이 없는 쪽은 슬라이더의 끝이다(최소는 0, 최대는 [PRICE_SLIDER_MAX]).
 * 슬라이더 범위를 벗어난 값은 끝으로 맞춘다.
 */
internal fun PriceFilter.toSliderRange(): ClosedFloatingPointRange<Float> {
    val start: Float = (min?.toFloat() ?: 0f).coerceIn(0f, PRICE_SLIDER_MAX)
    val end: Float = (max?.toFloat() ?: PRICE_SLIDER_MAX).coerceIn(0f, PRICE_SLIDER_MAX)
    // 최소가 최대보다 큰 필터가 들어와도 슬라이더는 시작이 끝보다 커질 수 없다
    return minOf(start, end)..end
}

/** 슬라이더의 선택 범위를 필터로 바꾼다. 끝에 닿은 쪽은 제한하지 않는다. (둘 다 끝이면 필터가 없다) */
internal fun ClosedFloatingPointRange<Float>.toPriceFilter(): PriceFilter = PriceFilter(
    min = start
        .takeIf { it > 0f }
        ?.roundToInt(),
    max = endInclusive
        .takeIf { it < PRICE_SLIDER_MAX }
        ?.roundToInt()
)
