package com.example.atuneslegacy.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val isDark: Boolean) {
    LEGACY_LIGHT("Legacy Light", isDark = false),
    LEGACY_DARK("Legacy Dark", isDark = true),
    NEW_LIGHT("New Light", isDark = false),
    NEW_DARK("New Dark", isDark = true)
}

data class ThemeColors(
    val appTheme: AppTheme,
    val isDark: Boolean,
    val primaryAccent: Color,
    val topBarStart: Color,
    val topBarEnd: Color,
    val topBarBorder: Color,
    val background: Color,
    val surface: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val inputText: Color, // Black for light themes, White for dark themes
    val activeSegmentStart: Color,
    val activeSegmentEnd: Color,
    val lcdBackground: Color,
    val lcdTextPrimary: Color,
    val lcdTextSecondary: Color
)

fun getThemeColors(appTheme: AppTheme): ThemeColors {
    return when (appTheme) {
        AppTheme.LEGACY_LIGHT -> ThemeColors(
            appTheme = appTheme,
            isDark = false,
            primaryAccent = Color(0xFF1C70E8),
            topBarStart = Color(0xFFE2E4E8),
            topBarEnd = Color(0xFFC0C4CB),
            topBarBorder = Color(0xFFA1A6B0),
            background = Color(0xFFF2F4F7),
            surface = Color(0xFFFFFFFF),
            surfaceBorder = Color(0xFFD0D4DD),
            textPrimary = Color(0xFF1C1D21),
            textSecondary = Color(0xFF636A78),
            inputText = Color.Black,
            activeSegmentStart = Color(0xFF5394ED),
            activeSegmentEnd = Color(0xFF1D6CE5),
            lcdBackground = Color(0xFF171A21),
            lcdTextPrimary = Color(0xFFE3E8F0),
            lcdTextSecondary = Color(0xFF919AA8)
        )
        AppTheme.LEGACY_DARK -> ThemeColors(
            appTheme = appTheme,
            isDark = true,
            primaryAccent = Color(0xFF388BFD),
            topBarStart = Color(0xFF32363C),
            topBarEnd = Color(0xFF1E2125),
            topBarBorder = Color(0xFF424750),
            background = Color(0xFF181A1D),
            surface = Color(0xFF24272C),
            surfaceBorder = Color(0xFF3A3F48),
            textPrimary = Color(0xFFF0F2F5),
            textSecondary = Color(0xFF9FA6B2),
            inputText = Color.White,
            activeSegmentStart = Color(0xFF388BFD),
            activeSegmentEnd = Color(0xFF1C70E8),
            lcdBackground = Color(0xFF0F1115),
            lcdTextPrimary = Color(0xFFF0F2F5),
            lcdTextSecondary = Color(0xFF888E9A)
        )
        AppTheme.NEW_LIGHT -> ThemeColors(
            appTheme = appTheme,
            isDark = false,
            primaryAccent = Color(0xFFFA233B), // Apple Music Red Accent
            topBarStart = Color(0xFFFFFFFF),
            topBarEnd = Color(0xFFF6F6F8),
            topBarBorder = Color(0xFFE5E5EA),
            background = Color(0xFFF2F2F7),
            surface = Color(0xFFFFFFFF),
            surfaceBorder = Color(0xFFE5E5EA),
            textPrimary = Color(0xFF000000),
            textSecondary = Color(0xFF8E8E93),
            inputText = Color.Black,
            activeSegmentStart = Color(0xFFFA233B),
            activeSegmentEnd = Color(0xFFD81B30),
            lcdBackground = Color(0xFFF6F6F8),
            lcdTextPrimary = Color(0xFF000000),
            lcdTextSecondary = Color(0xFF8E8E93)
        )
        AppTheme.NEW_DARK -> ThemeColors(
            appTheme = appTheme,
            isDark = true,
            primaryAccent = Color(0xFFFA2D48), // Apple Music Red Accent
            topBarStart = Color(0xFF1C1C1E),
            topBarEnd = Color(0xFF111111),
            topBarBorder = Color(0xFF2C2C2E),
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1E),
            surfaceBorder = Color(0xFF2C2C2E),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF8E8E93),
            inputText = Color.White,
            activeSegmentStart = Color(0xFFFA2D48),
            activeSegmentEnd = Color(0xFFD81B30),
            lcdBackground = Color(0xFF1C1C1E),
            lcdTextPrimary = Color(0xFFFFFFFF),
            lcdTextSecondary = Color(0xFF8E8E93)
        )
    }
}

// iTunes 11 Silver / Metallic Palette Constants
val ITunesTopBarStart = Color(0xFFE2E4E8)
val ITunesTopBarEnd = Color(0xFFC0C4CB)
val ITunesTopBarBorder = Color(0xFFA1A6B0)

val ITunesDarkTopBarStart = Color(0xFF32363C)
val ITunesDarkTopBarEnd = Color(0xFF1E2125)
val ITunesDarkTopBarBorder = Color(0xFF121417)

// LCD Track Display
val ITunesLcdBackground = Color(0xFF171A21)
val ITunesLcdBorder = Color(0xFF2C313C)
val ITunesLcdTextPrimary = Color(0xFFE3E8F0)
val ITunesLcdTextSecondary = Color(0xFF919AA8)
val ITunesLcdProgressTrack = Color(0xFF2B313D)
val ITunesLcdProgressFill = Color(0xFF388BFD)

// Segmented Control & Accent Colors
val ITunesAccentBlue = Color(0xFF1C70E8)
val ITunesAccentBlueHover = Color(0xFF155EC4)
val ITunesActiveSegmentStart = Color(0xFF5394ED)
val ITunesActiveSegmentEnd = Color(0xFF1D6CE5)

// Metallic Background & Card Colors
val ITunesBackgroundLight = Color(0xFFECEEF1)
val ITunesBackgroundDark = Color(0xFF181A1D)
val ITunesSurfaceLight = Color(0xFFFFFFFF)
val ITunesSurfaceDark = Color(0xFF24272C)

val ITunesDividerLight = Color(0xFFD0D4DC)
val ITunesDividerDark = Color(0xFF31363E)

val ITunesTextPrimaryLight = Color(0xFF1C1D21)
val ITunesTextSecondaryLight = Color(0xFF636A78)
val ITunesTextPrimaryDark = Color(0xFFF0F2F5)
val ITunesTextSecondaryDark = Color(0xFF9FA6B2)
