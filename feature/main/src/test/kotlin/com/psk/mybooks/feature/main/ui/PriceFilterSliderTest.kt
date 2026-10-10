package com.psk.mybooks.feature.main.ui

import com.psk.mybooks.core.model.PriceFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFilterSliderTest {

    @Test
    fun `필터가 없으면 슬라이더는 양 끝까지고 양 끝이면 필터가 없다`() {
        assertEquals(0f..PRICE_SLIDER_MAX, PriceFilter().toSliderRange())
        assertEquals(PriceFilter(), (0f..PRICE_SLIDER_MAX).toPriceFilter())
    }

    @Test
    fun `최소만 있으면 오른쪽은 끝이고 최대만 있으면 왼쪽은 끝이다`() {
        assertEquals(30_000f..PRICE_SLIDER_MAX, PriceFilter(min = 30_000).toSliderRange())
        assertEquals(0f..20_000f, PriceFilter(max = 20_000).toSliderRange())
    }

    @Test
    fun `끝에 닿은 쪽은 제한하지 않고 안쪽이면 그 금액으로 제한한다`() {
        assertEquals(PriceFilter(min = 10_000), (10_000f..PRICE_SLIDER_MAX).toPriceFilter())
        assertEquals(PriceFilter(max = 50_000), (0f..50_000f).toPriceFilter())
        assertEquals(PriceFilter(min = 10_000, max = 50_000), (10_000f..50_000f).toPriceFilter())
    }

    @Test
    fun `필터를 슬라이더로 바꿨다가 되돌리면 같은 필터다`() {
        listOf(
            PriceFilter(),
            PriceFilter(min = 5_000),
            PriceFilter(max = 99_000),
            PriceFilter(min = 1_000, max = 99_000),
            PriceFilter(min = 38_000, max = 62_000)
        ).forEach { filter ->
            assertEquals(filter, filter.toSliderRange().toPriceFilter())
        }
    }

    @Test
    fun `슬라이더 범위를 벗어난 값은 끝으로 맞춘다`() {
        assertEquals(PRICE_SLIDER_MAX..PRICE_SLIDER_MAX, PriceFilter(min = 250_000).toSliderRange())
        assertEquals(0f..PRICE_SLIDER_MAX, PriceFilter(max = 250_000).toSliderRange())
    }

    @Test
    fun `최소가 최대보다 큰 필터도 시작이 끝보다 커지지 않는다`() {
        val range: ClosedFloatingPointRange<Float> = PriceFilter(min = 60_000, max = 20_000).toSliderRange()

        assertEquals(20_000f..20_000f, range)
    }

    @Test
    fun `눈금 수는 양 끝을 뺀 칸 수다`() {
        assertEquals(99, PRICE_SLIDER_STEPS)
    }
}
