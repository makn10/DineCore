package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val cardBackground: Color,
    val screenBackground: Color,
    val border: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val tealTint: Color,
    val rateTint: Color,
    val rateText: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        cardBackground = AppColors.CardLight,
        screenBackground = AppColors.BackgroundLight,
        border = AppColors.BorderLight,
        textPrimary = AppColors.TextDark,
        textMuted = AppColors.TextMuted,
        tealTint = AppColors.TealTint,
        rateTint = AppColors.RateTint,
        rateText = AppColors.RateText
    )
}

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.TealTint,
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.PrimaryDark,
    onSecondary = Color.White,
    tertiary = AppColors.Indigo,
    background = AppColors.BackgroundLight,
    surface = AppColors.CardLight,
    onSurface = AppColors.TextDark,
    error = AppColors.Negative,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = Color.White,
    primaryContainer = AppColors.TealTintDark,
    onPrimaryContainer = AppColors.TealTint,
    secondary = AppColors.DeepTeal,
    onSecondary = Color.White,
    tertiary = AppColors.Indigo,
    background = AppColors.BackgroundDark,
    surface = AppColors.CardDark,
    onSurface = AppColors.TextLight,
    error = AppColors.Negative,
    onError = Color.White
)

@Composable
fun SmartMessTheme(
    themeMode: String = "system", // "system", "light", "dark"
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode.lowercase()) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val extendedColors = if (isDark) {
        ExtendedColors(
            cardBackground = AppColors.CardDark,
            screenBackground = AppColors.BackgroundDark,
            border = AppColors.BorderDark,
            textPrimary = AppColors.TextLight,
            textMuted = AppColors.TextMuted,
            tealTint = AppColors.TealTintDark,
            rateTint = AppColors.RateTintDark,
            rateText = AppColors.RateTextDark
        )
    } else {
        ExtendedColors(
            cardBackground = AppColors.CardLight,
            screenBackground = AppColors.BackgroundLight,
            border = AppColors.BorderLight,
            textPrimary = AppColors.TextDark,
            textMuted = AppColors.TextMuted,
            tealTint = AppColors.TealTint,
            rateTint = AppColors.RateTint,
            rateText = AppColors.RateText
        )
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
