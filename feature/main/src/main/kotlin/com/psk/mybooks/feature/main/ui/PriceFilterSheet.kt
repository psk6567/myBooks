package com.psk.mybooks.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.component.ActionButton
import com.psk.mybooks.core.designsystem.component.MbRangeSlider
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.VSpacer
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.feature.main.R
import java.text.NumberFormat

/**
 * 슬라이더로 금액 범위를 골라 즐겨찾기를 거르는 시트 내용. 0 ~ 100,000원을 1,000원 단위로 고르고,
 * 양 끝은 제한이 없다는 뜻이다(왼쪽 끝은 최소 없음, 오른쪽 끝은 "100,000원 이상"). 초기화는 필터를 없애고 닫는다.
 *
 * 두 손잡이가 서로 넘어갈 수 없어서 최소가 최대보다 커지는 일이 없다. 정확한 금액을 직접 고를 수는 없고 1,000원 단위로만 움직인다.
 *
 * @param filter 지금 걸려 있는 필터. 시트가 열릴 때 슬라이더의 처음 위치가 된다.
 * @param onApply 적용(또는 초기화)할 필터. 호출한 쪽이 시트를 닫는다.
 */
@Composable
internal fun PriceFilterSheet(filter: PriceFilter, onApply: (PriceFilter) -> Unit, modifier: Modifier = Modifier) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography
    val initial: ClosedFloatingPointRange<Float> = filter.toSliderRange()
    var start: Float by rememberSaveable { mutableFloatStateOf(initial.start) }
    var end: Float by rememberSaveable { mutableFloatStateOf(initial.endInclusive) }
    val selected: PriceFilter = (start..end).toPriceFilter()
    val format: NumberFormat = NumberFormat.getNumberInstance()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_main_price_filter),
            style = typography.bold16,
            color = colors.textPrimary
        )
        VSpacer(16.dp)
        // 고른 범위를 글자로 보여 준다. 슬라이더만으로는 정확한 금액을 알기 어렵다.
        Text(
            text = if (selected.isActive) {
                priceFilterLabel(
                    selected
                )
            } else {
                stringResource(R.string.feature_main_price_filter_all)
            },
            style = typography.bold14,
            color = colors.brand,
            modifier = Modifier.fillMaxWidth()
        )
        VSpacer(8.dp)
        MbRangeSlider(
            value = start..end,
            onValueChange = { range ->
                start = range.start
                end = range.endInclusive
            },
            valueRange = 0f..PRICE_SLIDER_MAX,
            steps = PRICE_SLIDER_STEPS
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.feature_main_price_slider_min_label),
                style = typography.normal12,
                color = colors.textTertiary
            )
            Text(
                text = stringResource(
                    R.string.feature_main_price_filter_min_only,
                    format.format(PRICE_SLIDER_MAX.toInt())
                ),
                style = typography.normal12,
                color = colors.textTertiary
            )
        }
        VSpacer(24.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionButton(
                text = stringResource(R.string.feature_main_price_filter_reset),
                onClick = { onApply(PriceFilter()) },
                primary = false,
                modifier = Modifier.weight(1f)
            )
            ActionButton(
                text = stringResource(R.string.feature_main_price_filter_apply),
                onClick = { onApply(selected) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 금액 칩에 보이는 글자. 필터가 없으면 "금액", 있으면 "1,000원 이상"처럼 범위를 보여 준다. */
@Composable
internal fun priceFilterLabel(filter: PriceFilter): String {
    val format: NumberFormat = NumberFormat.getNumberInstance()
    val min: Int? = filter.min
    val max: Int? = filter.max
    return when {
        min != null && max != null ->
            stringResource(R.string.feature_main_price_filter_range, format.format(min), format.format(max))

        min != null -> stringResource(R.string.feature_main_price_filter_min_only, format.format(min))

        max != null -> stringResource(R.string.feature_main_price_filter_max_only, format.format(max))

        else -> stringResource(R.string.feature_main_price_filter)
    }
}

@Preview(name = "PriceFilterSheet", widthDp = 360)
@Composable
private fun PriceFilterSheetPreview() {
    MyBooksTheme {
        PriceFilterSheet(filter = PriceFilter(min = 10000, max = 20000), onApply = {})
    }
}

@Preview(name = "PriceFilterSheet invalid (Dark)", widthDp = 360)
@Composable
private fun PriceFilterSheetInvalidPreview() {
    MyBooksTheme(darkTheme = true) {
        PriceFilterSheet(filter = PriceFilter(min = 30000, max = 20000), onApply = {})
    }
}
