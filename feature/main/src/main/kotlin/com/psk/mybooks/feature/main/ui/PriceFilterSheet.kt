package com.psk.mybooks.feature.main.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.component.ActionButton
import com.psk.mybooks.core.designsystem.component.AmountField
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MbTypography
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme
import com.psk.mybooks.core.designsystem.util.HSpacer
import com.psk.mybooks.core.designsystem.util.VSpacer
import com.psk.mybooks.core.model.PriceFilter
import com.psk.mybooks.feature.main.R
import java.text.NumberFormat

// TODO: 최소/최대 금액 입력 대신 범위 슬라이더(RangeSlider)로 금액을 고르는 방식을 검토한다.
//  슬라이더는 입력 검증(최소 > 최대)이 필요 없고 조작이 쉽지만, 정확한 금액을 고르기 어렵고
//  슬라이더 범위(즐겨찾기의 최저가~최고가)를 정해야 한다. 바꾸게 되면 AmountField 는 쓰지 않을 수 있다.
/**
 * 최소·최대 금액을 입력해 즐겨찾기를 거르는 시트 내용. 비워 둔 쪽은 제한하지 않는다.
 * 최소가 최대보다 크면 안내를 보여 주고 적용할 수 없다. 초기화는 필터를 없애고 닫는다.
 *
 * @param filter 지금 걸려 있는 필터. 시트가 열릴 때 입력창의 처음 값이 된다.
 * @param onApply 적용(또는 초기화)할 필터. 호출한 쪽이 시트를 닫는다.
 */
@Composable
internal fun PriceFilterSheet(filter: PriceFilter, onApply: (PriceFilter) -> Unit, modifier: Modifier = Modifier) {
    val colors: MbColors = MyBooksTheme.colors
    val typography: MbTypography = MyBooksTheme.typography
    var minText: String by rememberSaveable { mutableStateOf(filter.min?.toString().orEmpty()) }
    var maxText: String by rememberSaveable { mutableStateOf(filter.max?.toString().orEmpty()) }
    val min: Int? = minText.toIntOrNull()
    val max: Int? = maxText.toIntOrNull()
    val isRangeValid: Boolean = min == null || max == null || min <= max

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.feature_main_price_filter_title),
            style = typography.bold16,
            color = colors.textPrimary
        )
        VSpacer(16.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            AmountField(
                value = minText,
                onValueChange = { minText = it },
                placeholder = stringResource(R.string.feature_main_price_filter_min),
                suffix = stringResource(R.string.feature_main_price_unit),
                modifier = Modifier.weight(1f)
            )
            HSpacer(8.dp)
            Text(
                text = stringResource(R.string.feature_main_price_filter_separator),
                style = typography.normal16,
                color = colors.textSecondary
            )
            HSpacer(8.dp)
            AmountField(
                value = maxText,
                onValueChange = { maxText = it },
                placeholder = stringResource(R.string.feature_main_price_filter_max),
                suffix = stringResource(R.string.feature_main_price_unit),
                modifier = Modifier.weight(1f)
            )
        }
        if (!isRangeValid) {
            VSpacer(8.dp)
            Text(
                text = stringResource(R.string.feature_main_price_filter_invalid),
                style = typography.normal12,
                color = colors.error
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
                onClick = { onApply(PriceFilter(min = min, max = max)) },
                enabled = isRangeValid,
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
