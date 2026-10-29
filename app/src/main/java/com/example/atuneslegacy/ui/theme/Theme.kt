package com.example.atuneslegacy.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalThemeColors = staticCompositionLocalOf {
    getThemeColors(AppTheme.LEGACY_LIGHT)
}

@Composable
fun ATunesLegacyTheme(
    appTheme: AppTheme = AppTheme.LEGACY_LIGHT,
    content: @Composable () -> Unit
) {
    val themeColors = getThemeColors(appTheme)

    val colorScheme = if (themeColors.isDark) {
        darkColorScheme(
            primary = themeColors.primaryAccent,
            onPrimary = themeColors.surface,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            outline = themeColors.surfaceBorder
        )
    } else {
        lightColorScheme(
            primary = themeColors.primaryAccent,
            onPrimary = themeColors.surface,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            outline = themeColors.surfaceBorder
        )
    }

    CompositionLocalProvider(LocalThemeColors provides themeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
