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
data class HubPalette(
    val bg: Color,
    val surface: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    val accent: Color,
    val accentInk: Color,
    val highlight: Color,
    val highlightInk: Color,
    val spend: Color,
    val earn: Color,
    val warn: Color,
    val done: Color,
    val isDark: Boolean
)

private val LightDefaultPalette = HubPalette(
    bg = HubColors.LightBg,
    surface = HubColors.LightSurface,
    ink = HubColors.LightInk,
    muted = HubColors.LightMuted,
    line = HubColors.LightLine,
    accent = HubColors.LightSchoolAccent,
    accentInk = HubColors.LightSchoolAccentInk,
    highlight = HubColors.Highlight,
    highlightInk = HubColors.HighlightInk,
    spend = HubColors.LightSpend,
    earn = HubColors.LightEarn,
    warn = HubColors.LightWarn,
    done = HubColors.LightDone,
    isDark = false
)

val LocalHubPalette = staticCompositionLocalOf { LightDefaultPalette }

enum class ThemeMode(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

@Composable
fun MyHubTheme(
    themeMode: ThemeMode,
    isMoneyMode: Boolean,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val palette = if (isDark) {
        HubPalette(
            bg = HubColors.DarkBg,
            surface = HubColors.DarkSurface,
            ink = HubColors.DarkInk,
            muted = HubColors.DarkMuted,
            line = HubColors.DarkLine,
            accent = if (isMoneyMode) HubColors.DarkMoneyAccent else HubColors.DarkSchoolAccent,
            accentInk = if (isMoneyMode) HubColors.DarkMoneyAccentInk else HubColors.DarkSchoolAccentInk,
            highlight = HubColors.Highlight,
            highlightInk = HubColors.HighlightInk,
            spend = HubColors.DarkSpend,
            earn = HubColors.DarkEarn,
            warn = HubColors.DarkWarn,
            done = HubColors.DarkDone,
            isDark = true
        )
    } else {
        HubPalette(
            bg = HubColors.LightBg,
            surface = HubColors.LightSurface,
            ink = HubColors.LightInk,
            muted = HubColors.LightMuted,
            line = HubColors.LightLine,
            accent = if (isMoneyMode) HubColors.LightMoneyAccent else HubColors.LightSchoolAccent,
            accentInk = if (isMoneyMode) HubColors.LightMoneyAccentInk else HubColors.LightSchoolAccentInk,
            highlight = HubColors.Highlight,
            highlightInk = HubColors.HighlightInk,
            spend = HubColors.LightSpend,
            earn = HubColors.LightEarn,
            warn = HubColors.LightWarn,
            done = HubColors.LightDone,
            isDark = false
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = palette.accentInk,
            background = palette.bg,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.bg,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
            error = palette.spend
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = palette.accentInk,
            background = palette.bg,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            surfaceVariant = palette.bg,
            onSurfaceVariant = palette.muted,
            outline = palette.line,
            error = palette.spend
        )
    }

    CompositionLocalProvider(LocalHubPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HubTypography,
            content = content
        )
    }
}
