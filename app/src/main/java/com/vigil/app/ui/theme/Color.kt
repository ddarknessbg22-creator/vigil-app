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
    val IcyCyan = Color(0xFFAADECB)
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

/** When false, surfaces render flat instead of layered glass. */
val LocalGlassEnabled = staticCompositionLocalOf { true }

val DarkScheme = darkColorScheme(
    primary = Color(0xFF5B9BFF),
    onPrimary = Color(0xFF00172E),
    primaryContainer = Color(0xFF16345F),
    onPrimaryContainer = Color(0xFFD7E6FF),
    secondary = Color(0xFFFF9A4D),
    onSecondary = Color(0xFF2B1300),
    tertiary = Color(0xFF4CD6A0),
    onTertiary = Color(0xFF00241A),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF2F3F5),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFF2F3F5),
    surfaceVariant = Color(0xFF1C1C1F),
    onSurfaceVariant = Color(0xFF9A9CA3),
    surfaceContainer = Color(0xFF1A1A1D),
    surfaceContainerHigh = Color(0xFF242427),
    surfaceContainerHighest = Color(0xFF2E2E32),
    outline = Color(0xFF3A3A3F),
    outlineVariant = Color(0xFF2A2A2E),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF2B0000)
)

val LightScheme = lightColorScheme(
    primary = Color(0xFF2F6FED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FF),
    onPrimaryContainer = Color(0xFF001B44),
    secondary = Color(0xFFF26A1B),
    onSecondary = Color.White,
    tertiary = Color(0xFF138A62),
    background = Color(0xFFF3F4F7),
    onBackground = Color(0xFF14151A),
    surface = Color(0xFFF3F4F7),
    onSurface = Color(0xFF14151A),
    surfaceVariant = Color(0xFFE7E9EE),
    onSurfaceVariant = Color(0xFF6B6F78),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFFFFFFF),
    surfaceContainerHighest = Color(0xFFECEEF2),
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
