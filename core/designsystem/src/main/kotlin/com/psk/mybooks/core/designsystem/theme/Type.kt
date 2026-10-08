package com.psk.mybooks.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.psk.mybooks.core.designsystem.R

/*
 * 여기어때 잘난체 2 (GC COMPANY, 개인·기업 무료 / 수정·재배포 허용)
 * 폰트 파일은 Regular 한 가지뿐이라 Bold 는 Compose 가 굵기를 합성(FontSynthesis)해서 그린다.
 */
internal val Jalnan = FontFamily(Font(R.font.jalnan2, FontWeight.Normal))

/**
 * 텍스트 스타일. 크기 10~24sp (2 단위) x normal / bold.
 * 화면과 컴포넌트는 `MyBooksTheme.typography.xxx` 만 사용한다. (fontSize 직접 지정 금지)
 */
@Immutable
data class MbTypography(
    val normal10: TextStyle,
    val bold10: TextStyle,
    val normal12: TextStyle,
    val bold12: TextStyle,
    val normal14: TextStyle,
    val bold14: TextStyle,
    val normal16: TextStyle,
    val bold16: TextStyle,
    val normal18: TextStyle,
    val bold18: TextStyle,
    val normal20: TextStyle,
    val bold20: TextStyle,
    val normal22: TextStyle,
    val bold22: TextStyle,
    val normal24: TextStyle,
    val bold24: TextStyle
)

private fun textStyle(size: Int, weight: FontWeight) = TextStyle(
    fontFamily = Jalnan,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp
)

internal val DefaultMbTypography = MbTypography(
    normal10 = textStyle(10, FontWeight.Normal),
    bold10 = textStyle(10, FontWeight.Bold),
    normal12 = textStyle(12, FontWeight.Normal),
    bold12 = textStyle(12, FontWeight.Bold),
    normal14 = textStyle(14, FontWeight.Normal),
    bold14 = textStyle(14, FontWeight.Bold),
    normal16 = textStyle(16, FontWeight.Normal),
    bold16 = textStyle(16, FontWeight.Bold),
    normal18 = textStyle(18, FontWeight.Normal),
    bold18 = textStyle(18, FontWeight.Bold),
    normal20 = textStyle(20, FontWeight.Normal),
    bold20 = textStyle(20, FontWeight.Bold),
    normal22 = textStyle(22, FontWeight.Normal),
    bold22 = textStyle(22, FontWeight.Bold),
    normal24 = textStyle(24, FontWeight.Normal),
    bold24 = textStyle(24, FontWeight.Bold)
)

internal val LocalMbTypography = staticCompositionLocalOf { DefaultMbTypography }
