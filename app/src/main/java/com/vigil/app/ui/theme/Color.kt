package com.vigil.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** VIGIL palette: deep black, graphite, charcoal, icy cyan, electric blue. */
object VigilPalette {
    val IcyCyan = Color(0xFF00E5FF)
    val ElectricBlue = Color(0xFF0091EA)
    val CoolWhite = Color(0xFFE8F4FD)

    val DeepBlack = Color(0xFF050607)
    val Graphite = Color(0xFF0E1113)
    val Charcoal = Color(0xFF161A1D)
    val Elevated = Color(0xFF1F2428)

    val Success = Color(0xFF00E676)
    val Warning = Color(0xFFFFC400)
    val Danger = Color(0xFFFF4D4D)

    val TextPrimary = Color(0xFFF5FAFF)
    val TextSecondary = Color(0xFFA9B6C2)
    val TextTertiary = Color(0xFF6E7A85)
}

/** Glass tokens, theme-aware. */
data class GlassTokens(
    val surfaceTint: Color,
    val surfaceTintStrong: Color,
    val borderTop: Color,
    val borderBottom: Color,
    val innerGlow: Color,
    val shadow: Color,
    val scrim: Color
)

val LocalGlassTokens = staticCompositionLocalOf {
    GlassTokens(
        surfaceTint = Color.White.copy(alpha = 0.07f),
        surfaceTintStrong = Color.White.copy(alpha = 0.12f),
        borderTop = Color.White.copy(alpha = 0.22f),
        borderBottom = Color.White.copy(alpha = 0.04f),
        innerGlow = Color.White.copy(alpha = 0.10f),
        shadow = Color.Black.copy(alpha = 0.55f),
        scrim = Color.Black.copy(alpha = 0.35f)
    )
}

val DarkScheme = darkColorScheme(
    primary = VigilPalette.IcyCyan,
    onPrimary = VigilPalette.DeepBlack,
    primaryContainer = VigilPalette.ElectricBlue,
    onPrimaryContainer = VigilPalette.DeepBlack,
    secondary = VigilPalette.ElectricBlue,
    onSecondary = VigilPalette.DeepBlack,
    tertiary = VigilPalette.Success,
    onTertiary = VigilPalette.DeepBlack,
    background = VigilPalette.DeepBlack,
    onBackground = VigilPalette.TextPrimary,
    surface = VigilPalette.Graphite,
    onSurface = VigilPalette.TextPrimary,
    surfaceVariant = VigilPalette.Charcoal,
    onSurfaceVariant = VigilPalette.TextSecondary,
    surfaceContainer = VigilPalette.Charcoal,
    surfaceContainerHigh = VigilPalette.Elevated,
    surfaceContainerHighest = VigilPalette.Elevated,
    outline = Color(0xFF2C343A),
    outlineVariant = Color(0xFF232A2F),
    error = VigilPalette.Danger,
    onError = VigilPalette.DeepBlack
)

val LightScheme = lightColorScheme(
    primary = Color(0xFF00668A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBEE9FF),
    onPrimaryContainer = Color(0xFF001E2B),
    secondary = Color(0xFF00639B),
    onSecondary = Color.White,
    tertiary = Color(0xFF00695C),
    background = Color(0xFFF7FAFC),
    onBackground = Color(0xFF101418),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101418),
    surfaceVariant = Color(0xFFE5EBF0),
    onSurfaceVariant = Color(0xFF414A52),
    surfaceContainer = Color(0xFFEDF2F6),
    surfaceContainerHigh = Color(0xFFE3E9EE),
    surfaceContainerHighest = Color(0xFFD8DFE5),
    outline = Color(0xFFB7C1C9),
    outlineVariant = Color(0xFFD2DAE0),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun VigilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = if (darkTheme) DarkScheme else LightScheme
    val tokens = if (darkTheme) {
        GlassTokens(
            surfaceTint = Color.White.copy(alpha = 0.075f),
            surfaceTintStrong = Color.White.copy(alpha = 0.13f),
            borderTop = Color.White.copy(alpha = 0.24f),
            borderBottom = Color.White.copy(alpha = 0.04f),
            innerGlow = Color.White.copy(alpha = 0.12f),
            shadow = Color.Black.copy(alpha = 0.6f),
            scrim = Color.Black.copy(alpha = 0.4f)
        )
    } else {
        GlassTokens(
            surfaceTint = Color.White.copy(alpha = 0.55f),
            surfaceTintStrong = Color.White.copy(alpha = 0.75f),
            borderTop = Color.White.copy(alpha = 0.95f),
            borderBottom = Color(0xFF9FB0BD).copy(alpha = 0.35f),
            innerGlow = Color.White.copy(alpha = 0.8f),
            shadow = Color(0xFF54636F).copy(alpha = 0.22f),
            scrim = Color.White.copy(alpha = 0.55f)
        )
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalGlassTokens provides tokens) {
        MaterialTheme(
            colorScheme = scheme,
            typography = VigilTypography,
            shapes = VigilShapes,
            content = content
        )
    }
}
