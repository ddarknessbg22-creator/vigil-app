package com.vigil.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Premium blue gradient — drawn once, never animated.
 *
 * A continuously animating gradient invalidates a draw node every frame even
 * when the user is idle. Rendering it once keeps the app at rest truly at
 * rest, which is the single biggest win for battery and sustained frame rate.
 */
fun Modifier.livingGradient(
    base: List<Color> = listOf(
        Color(0xFF0A1E3D),
        Color(0xFF103A76),
        Color(0xFF0A5FD0),
        Color(0xFF0A1E3D)
    ),
    glow: Color = Color(0xFF4C9DFF),
    @Suppress("UNUSED_PARAMETER") periodMs: Int = 9000
): Modifier = this.drawBehind {
    val w = size.width
    val h = size.height

    drawRect(
        brush = Brush.linearGradient(
            colors = base,
            start = androidx.compose.ui.geometry.Offset(-w * 0.25f, 0f),
            end = androidx.compose.ui.geometry.Offset(w * 1.25f, h)
        )
    )
    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(glow.copy(alpha = 0.18f), Color.Transparent),
            center = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.18f),
            radius = w * 0.75f
        )
    )
}
