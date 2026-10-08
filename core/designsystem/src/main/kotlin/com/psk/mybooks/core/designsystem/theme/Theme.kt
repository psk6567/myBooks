package com.psk.mybooks.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

@Composable
fun MyBooksTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkMbColors else LightMbColors
    val colorScheme = if (darkTheme) darkScheme(colors) else lightScheme(colors)

    val typography = DefaultMbTypography

    CompositionLocalProvider(
        LocalMbColors provides colors,
        LocalMbTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = materialTypography(typography),
            content = content
        )
    }
}

object MyBooksTheme {
    val colors: MbColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMbColors.current

    val typography: MbTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalMbTypography.current
}

/*
 * Material Typography 매핑. Material 컴포넌트(Text 기본값, 버튼, 메뉴 등)도 같은 폰트를 쓰도록 연결만 한다.
 * display/headline Large·Medium 은 24sp 를 넘어 스케일 밖이라 매핑하지 않는다.
 */
private fun materialTypography(t: MbTypography) = Typography(
    headlineSmall = t.bold24,
    titleLarge = t.bold22,
    titleMedium = t.bold16,
    titleSmall = t.bold14,
    bodyLarge = t.normal16,
    bodyMedium = t.normal14,
    bodySmall = t.normal12,
    labelLarge = t.bold14,
    labelMedium = t.bold12,
    labelSmall = t.bold10
)

/*
 * Material ColorScheme 매핑.
 * Material 컴포넌트(DropdownMenu, ripple 등)가 같은 톤을 내도록 연결만 한다.
 * 화면 코드에서 MaterialTheme.colorScheme 을 직접 쓰지 않는다.
 */
private fun lightScheme(colors: MbColors): ColorScheme = lightColorScheme(
    primary = colors.brand,
    onPrimary = colors.textOnBrand,
    primaryContainer = Red49,
    onPrimaryContainer = Red99,
    inversePrimary = Red80,
    secondary = Rose40,
    onSecondary = Gray100,
    secondaryContainer = Rose67,
    onSecondaryContainer = Rose24,
    tertiary = Teal39,
    onTertiary = Gray100,
    tertiaryContainer = Teal49,
    onTertiaryContainer = Teal99,
    error = colors.error,
    onError = Gray100,
    errorContainer = Error90,
    onErrorContainer = Error30,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.background,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.textSecondary,
    surfaceTint = colors.brand,
    inverseSurface = Gray20,
    inverseOnSurface = Gray95,
    outline = colors.border,
    outlineVariant = colors.divider,
    scrim = Gray0,
    surfaceBright = Gray98,
    surfaceDim = Gray87,
    surfaceContainerLowest = Gray100,
    surfaceContainerLow = Gray96,
    surfaceContainer = Gray94,
    surfaceContainerHigh = Gray92,
    surfaceContainerHighest = Gray90
)

private fun darkScheme(colors: MbColors): ColorScheme = darkColorScheme(
    primary = colors.brand,
    onPrimary = colors.textOnBrand,
    primaryContainer = Red60,
    onPrimaryContainer = Red10,
    inversePrimary = Red40,
    secondary = Red80,
    onSecondary = Rose20,
    secondaryContainer = Rose31,
    onSecondaryContainer = Rose75,
    tertiary = Teal80,
    onTertiary = Teal20,
    tertiaryContainer = Teal60,
    onTertiaryContainer = Teal11,
    error = colors.error,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = colors.background,
    onBackground = colors.textPrimary,
    surface = colors.background,
    onSurface = colors.textPrimary,
    surfaceVariant = colors.surfaceVariant,
    onSurfaceVariant = colors.textSecondary,
    surfaceTint = colors.brand,
    inverseSurface = Gray90,
    inverseOnSurface = Gray20,
    outline = colors.border,
    outlineVariant = colors.divider,
    scrim = Gray0,
    surfaceBright = Gray24,
    surfaceDim = Gray6,
    surfaceContainerLowest = Gray4,
    surfaceContainerLow = Gray10,
    surfaceContainer = Gray12,
    surfaceContainerHigh = Gray17,
    surfaceContainerHighest = Gray22
)
