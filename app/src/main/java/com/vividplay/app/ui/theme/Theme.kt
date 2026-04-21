package com.vividplay.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = ClaudeClay,
    onPrimary = ClaudeCream,
    primaryContainer = ClaudeMist,
    onPrimaryContainer = ClaudeInk,
    secondary = ClaudeLeaf,
    onSecondary = ClaudeCream,
    tertiary = ClaudeAccent,
    background = ClaudeCream,
    onBackground = ClaudeInk,
    surface = ClaudeParchment,
    onSurface = ClaudeInk,
    surfaceVariant = ClaudeMist,
    onSurfaceVariant = ClaudeGraphite,
    outline = ClaudeStone,
)

private val DarkColors = darkColorScheme(
    primary = ClaudeClay,
    onPrimary = ClaudeInk,
    primaryContainer = ClaudeClayDim,
    onPrimaryContainer = ClaudeCream,
    secondary = ClaudeLeaf,
    onSecondary = ClaudeInk,
    tertiary = ClaudeAccent,
    background = DarkBackdrop,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = DarkOnSurfaceDim,
    outline = ClaudeStone,
)

@Composable
fun VividPlayTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Off by default — we want the Claude aesthetic, not Monet.
    content: @Composable () -> Unit
) {
    val scheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = scheme, typography = VividTypography, content = content)
}
