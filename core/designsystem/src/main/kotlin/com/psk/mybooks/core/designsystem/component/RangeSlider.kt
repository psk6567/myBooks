package com.psk.mybooks.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.psk.mybooks.core.designsystem.theme.MbColors
import com.psk.mybooks.core.designsystem.theme.MyBooksTheme

/**
 * 두 손잡이로 범위를 고르는 슬라이더. 두 손잡이는 서로 넘어갈 수 없어서 시작이 끝보다 커지는 일이 없다.
 * 선택한 범위 안은 브랜드 색으로 채워진다. 현재 값은 이 컴포넌트가 보여 주지 않으므로 호출하는 쪽이 글자로 함께 보여 준다.
 * [BottomSheet] 안에서 쓰면 누르고 있는 동안 시트의 끌기를 잠가 시트가 같이 흔들리지 않는다.
 *
 * @param value 선택한 범위
 * @param valueRange 고를 수 있는 전체 범위
 * @param steps [valueRange] 를 나누는 눈금 수(양 끝 제외). 0 이면 연속으로 움직인다. 눈금 표시는 그리지 않고 그 칸으로만 움직인다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MbRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0
) {
    val colors: MbColors = MyBooksTheme.colors
    val sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = colors.brand,
        activeTrackColor = colors.brand,
        inactiveTrackColor = colors.border,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent
    )

    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        // 시트 안에서 쓰면 끄는 동안 시트가 같이 흔들리지 않도록 시트의 끌기를 잠근다
        modifier = modifier
            .fillMaxWidth()
            .lockBottomSheetWhilePressed(),
        valueRange = valueRange,
        steps = steps,
        colors = sliderColors
    )
}

@Preview(name = "RangeSlider", widthDp = 360)
@Composable
private fun MbRangeSliderPreview() {
    MyBooksTheme {
        var range: ClosedFloatingPointRange<Float> by remember { mutableStateOf(20f..70f) }
        MbRangeSlider(
            value = range,
            onValueChange = { range = it },
            valueRange = 0f..100f,
            steps = 9,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "RangeSlider (Dark)", widthDp = 360)
@Composable
private fun MbRangeSliderDarkPreview() {
    MyBooksTheme(darkTheme = true) {
        var range: ClosedFloatingPointRange<Float> by remember { mutableStateOf(0f..40f) }
        MbRangeSlider(
            value = range,
            onValueChange = { range = it },
            valueRange = 0f..100f,
            steps = 9,
            modifier = Modifier.padding(16.dp)
        )
    }
}
