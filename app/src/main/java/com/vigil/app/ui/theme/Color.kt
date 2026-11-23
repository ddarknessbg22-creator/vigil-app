package com.vigil.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * MIUI / HyperOS-inspired palette.
 *
 * Page and card are deliberately close in value: the separation comes from a
 * small step in lightness and a large corner radius, not from outlines or
 * shadows — the way Xiaomi's settings and notes surfaces are built.
 */
object VigilPalette {
    val IcyCyan = Color(0xFF9CC8FF)
    val ElectricBlue = Color(0xFF3B82F6)
    val CoolWhite = Color(0xFFF2F4F8)

    val DeepBlack = Color(0xFF0B0B0E)
    val Graphite = Color(0xFF0F0F12)
    val Charcoal = Color(0xFF1C1C21)
    val Elevated = Color(0xFF24242A)

    val Success = Color(0xFF4CD6A0)
    val Warning = Color(0xFFFFB020)
    val Danger = Color(0xFFFF6B6B)

    val TextPrimary = Color(0xFFF5F6F8)
    val TextSecondary = Color(0xFFA0A2AC)
    val TextTertiary = Color(0xFF6E707A)
}

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

/** When false, surfaces render flat instead of layered glass. */
val LocalGlassEnabled = staticCompositionLocalOf { true }

val DarkScheme = darkColorScheme(
    primary = Color(0xFF4C8DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B3A6B),
    onPrimaryContainer = Color(0xFFD9E7FF),
    secondary = Color(0xFFFF9A4D),
    onSecondary = Color(0xFF2B1300),
    tertiary = Color(0xFF4CD6A0),
    onTertiary = Color(0xFF00241A),
    background = VigilPalette.Graphite,
    onBackground = VigilPalette.TextPrimary,
    surface = VigilPalette.Graphite,
    onSurface = VigilPalette.TextPrimary,
    surfaceVariant = VigilPalette.Charcoal,
    onSurfaceVariant = VigilPalette.TextSecondary,
    surfaceContainerLowest = VigilPalette.DeepBlack,
    surfaceContainerLow = VigilPalette.Graphite,
    surfaceContainer = VigilPalette.Charcoal,
    surfaceContainerHigh = VigilPalette.Elevated,
    surfaceContainerHighest = Color(0xFF2E2E35),
    outline = Color(0xFF3A3A42),
    outlineVariant = Color(0xFF2A2A31),
    error = VigilPalette.Danger,
    onError = Color(0xFF2B0000)
)

val LightScheme = lightColorScheme(
    primary = Color(0xFF0A59F7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBE7FF),
    onPrimaryContainer = Color(0xFF001B44),
    secondary = Color(0xFFE86A18),
    onSecondary = Color.White,
    tertiary = Color(0xFF0F8A62),
    background = Color(0xFFF2F3F7),
    onBackground = Color(0xFF14151A),
    surface = Color(0xFFF2F3F7),
    onSurface = Color(0xFF14151A),
    surfaceVariant = Color(0xFFE7E9EE),
    onSurfaceVariant = Color(0xFF666973),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFAFAFC),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF7F8FA),
    surfaceContainerHighest = Color(0xFFEFF0F4),
    outline = Color(0xFFC8CBD2),
    outlineVariant = Color(0xFFE2E4E9),
    error = Color(0xFFD93636),
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
