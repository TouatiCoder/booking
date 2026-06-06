package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LuxuryGold,
    onPrimary = LuxuryDarkBlue,
    secondary = LuxuryZelligeGreen,
    onSecondary = LuxuryWhite,
    tertiary = LuxuryLightGold,
    onTertiary = LuxuryDarkBlue,
    background = LuxuryDarkBlue,
    onBackground = LuxuryWhite,
    surface = Color(0xFF13324E),
    onSurface = LuxuryWhite,
    surfaceVariant = Color(0xFF0F2C46),
    onSurfaceVariant = LuxuryLightGold,
    outline = LuxuryGold
)

private val LightColorScheme = lightColorScheme(
    primary = LuxuryDarkBlue,
    onPrimary = LuxuryWhite,
    secondary = LuxuryGold,
    onSecondary = LuxuryDarkBlue,
    tertiary = LuxuryZelligeGreen,
    onTertiary = LuxuryWhite,
    background = LuxurySand,
    onBackground = TextDark,
    surface = CardBackground,
    onSurface = TextDark,
    surfaceVariant = LuxurySand,
    onSurfaceVariant = TextDark,
    outline = BorderLight
)

@Composable
fun ZelligeStaysTheme(
    darkTheme: Boolean = false, // We prioritize light brand theme for classic tile-ceramic feeling, but allow dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
