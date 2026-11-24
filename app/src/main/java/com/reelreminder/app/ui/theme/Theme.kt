package com.reelreminder.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF042F2E),
    secondary = TealDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF082A29),
    background = LightB1,
    onBackground = LightInk,
    surface = LightGlass,
    onSurface = LightInk,
    surfaceVariant = LightChip,
    onSurfaceVariant = LightMute,
    outline = LightEdge,
    error = RedDot,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color(0xFF04201F),
    primaryContainer = Color(0xFF134E4A),
    onPrimaryContainer = Color(0xFFCCFBF1),
    secondary = TealDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0A2523),
    onSecondaryContainer = DarkInk,
    background = DarkB1,
    onBackground = DarkInk,
    surface = DarkGlass,
    onSurface = DarkInk,
    surfaceVariant = DarkChip,
    onSurfaceVariant = DarkMute,
    outline = DarkEdge,
    error = RedDot,
    onError = Color.White
)

@Composable
fun ReelReminderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
