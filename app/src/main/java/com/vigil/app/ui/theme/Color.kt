package com.vigil.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Premium, iOS-inspired palette.
 *
 * Neutral layered surfaces, one electric accent, no tonal colour casts.
 * rounded grouped cards, hairline separators and restrained highlights do
 * the work that gradients used to.
 */
object VigilPalette {
    val IcyCyan = Color(0xFF0A84FF)
    val ElectricBlue = Color(0xFF0A84FF)
    val CoolWhite = Color(0xFFF2F2F7)

    val DeepBlack = Color(0xFF000000)
    val Graphite = Color(0xFF000000)
    val Charcoal = Color(0xFF1C1C1E)
    val Elevated = Color(0xFF2C2C2E)

    val Success = Color(0xFF30D158)
    val Warning = Color(0xFFFF9F0A)
    val Danger = Color(0xFFFF453A)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF98989F)
    val TextTertiary = Color(0xFF6E6E73)
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

val LocalGlassEnabled = staticCompositionLocalOf { true }

val DarkScheme = darkColorScheme(
    primary = Color(0xFF0A84FF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1C3A5E),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFFFF9F0A),
    onSecondary = Color(0xFF2B1600),
    tertiary = Color(0xFF30D158),
    onTertiary = Color(0xFF00220C),
    background = VigilPalette.DeepBlack,
    onBackground = Color.White,
    surface = VigilPalette.DeepBlack,
    onSurface = Color.White,
    surfaceVariant = VigilPalette.Charcoal,
    onSurfaceVariant = VigilPalette.TextSecondary,
    surfaceContainerLowest = Color(0xFF0A0A0A),
    surfaceContainerLow = Color(0xFF141416),
    surfaceContainer = VigilPalette.Charcoal,
    surfaceContainerHigh = VigilPalette.Elevated,
    surfaceContainerHighest = Color(0xFF3A3A3C),
    outline = Color(0xFF38383A),
    outlineVariant = Color(0xFF2C2C2E),
    error = VigilPalette.Danger,
    onError = Color(0xFF2B0000)
)

val LightScheme = lightColorScheme(
    primary = Color(0xFF007AFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCEBFF),
    onPrimaryContainer = Color(0xFF001E44),
    secondary = Color(0xFFFF9500),
    onSecondary = Color.White,
    tertiary = Color(0xFF34C759),
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7),
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF6E6E73),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF9F9FB),
    surfaceContainer = Color.White,
    surfaceContainerHigh = Color(0xFFF2F2F7),
    surfaceContainerHighest = Color(0xFFE5E5EA),
    outline = Color(0xFFC7C7CC),
    outlineVariant = Color(0xFFE5E5EA),
    error = Color(0xFFFF3B30),
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
            surfaceTint = Color.White.copy(alpha = 0.05f),
            surfaceTintStrong = Color.White.copy(alpha = 0.09f),
            borderTop = Color.White.copy(alpha = 0.14f),
            borderBottom = Color.White.copy(alpha = 0.03f),
            innerGlow = Color.White.copy(alpha = 0.08f),
            shadow = Color.Black.copy(alpha = 0.5f),
            scrim = Color.Black.copy(alpha = 0.35f)
        )
    } else {
        GlassTokens(
            surfaceTint = Color.White.copy(alpha = 0.6f),
            surfaceTintStrong = Color.White.copy(alpha = 0.8f),
            borderTop = Color.White.copy(alpha = 0.9f),
            borderBottom = Color(0xFFC7C7CC).copy(alpha = 0.3f),
            innerGlow = Color.White.copy(alpha = 0.7f),
            shadow = Color(0xFF3C3C43).copy(alpha = 0.14f),
            scrim = Color.White.copy(alpha = 0.5f)
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
