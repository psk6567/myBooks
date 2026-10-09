package com.psk.mybooks.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic Colors. 색의 "값"이 아니라 "용도"로 이름을 붙인다.
 * 화면과 컴포넌트는 `MyBooksTheme.colors.xxx` 만 사용한다. (토큰, MaterialTheme.colorScheme 직접 사용 금지)
 */
@Immutable
data class MbColors(
    // 배경
    val background: Color,
    /** 카드 (리스트 아이템 등). Material 의 surface(바닥층)와는 다른 의미라 card 로 이름 붙임 */
    val card: Color,
    /** 검색창, 썸네일 자리, 책 소개 박스 */
    val surfaceVariant: Color,
    /** 리스트 영역 배경 */
    val listBackground: Color,

    // 글자
    val textPrimary: Color,
    val textSecondary: Color,
    /** 플레이스홀더, 보조 라벨 */
    val textTertiary: Color,
    /** brand 배경 위 글자 */
    val textOnBrand: Color,

    // 아이콘
    val icon: Color,
    val iconInactive: Color,

    // 경계
    val border: Color,
    val divider: Color,

    // 브랜드
    val brand: Color,
    /** 즐겨찾기(하트) 활성 */
    val favorite: Color,
    /** 정가와 다른 판매가 강조 */
    val salePrice: Color,

    // 상태
    val error: Color,
    /** error 배경 위 글자·아이콘 */
    val onError: Color,
    val success: Color,
    /** success 배경 위 글자·아이콘 */
    val onSuccess: Color,

    // 이미지 위
    /** 표지 같은 이미지 위에 글자를 올릴 때 깔아 글자가 읽히게 하는 어두운 반투명 막. 라이트·다크 모두 같다. */
    val imageOverlay: Color,
    /** [imageOverlay] 위의 글자 */
    val onImageOverlay: Color,

    // 스낵바
    /** 종류가 없는(None) 스낵바 배경 */
    val snackbar: Color,
    val onSnackbar: Color
)

internal val LightMbColors: MbColors = MbColors(
    background = Gray100,
    card = Gray100,
    surfaceVariant = Gray95,
    listBackground = Gray92,
    textPrimary = Gray10,
    textSecondary = Gray30,
    textTertiary = Gray50,
    textOnBrand = Gray100,
    icon = Gray10,
    iconInactive = Gray60,
    border = Gray87,
    divider = Gray90,
    brand = Red55,
    favorite = Red55,
    salePrice = Red55,
    error = Error40,
    onError = Gray100,
    success = Green40,
    onSuccess = Gray100,
    snackbar = Gray10,
    onSnackbar = Gray100,
    imageOverlay = Scrim,
    onImageOverlay = Gray100
)

internal val DarkMbColors: MbColors = MbColors(
    background = Gray6,
    card = Gray12,
    surfaceVariant = Gray17,
    listBackground = Gray4,
    textPrimary = Gray90,
    textSecondary = Gray80,
    textTertiary = Gray60,
    textOnBrand = Gray100,
    icon = Gray90,
    iconInactive = Gray50,
    border = Gray30,
    divider = Gray22,
    brand = Red55,
    favorite = Red60,
    salePrice = Red60,
    error = Error80,
    onError = Error20,
    success = Green80,
    onSuccess = Green20,
    snackbar = Gray90,
    onSnackbar = Gray10,
    imageOverlay = Scrim,
    onImageOverlay = Gray100
)

internal val LocalMbColors: ProvidableCompositionLocal<MbColors> = staticCompositionLocalOf { LightMbColors }
