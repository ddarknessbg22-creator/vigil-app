package com.vigil.app.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/**
 * A slow, living blue gradient that animates entirely in the draw phase.
 * No state is read during composition, so scrolling and animation never
 * trigger recomposition.
 */
fun Modifier.livingGradient(
    base: List<Color> = listOf(
        Color(0xFF0A1E3D),
        Color(0xFF103A76),
        Color(0xFF0A5FD0),
        Color(0xFF0A1E3D)
    ),
    glow: Color = Color(0xFF4C9DFF),
    periodMs: Long = 9000L
): Modifier = this.drawBehind {
    val time = withInfiniteAnimationFrameMillis { it }
    val phase = (time % periodMs).toFloat() / periodMs.toFloat()
    val drift = (sin(phase * 2f * PI) + 1f) / 2f

    drawRect(
        brush = Brush.linearGradient(
            colors = base,
            start = Offset(x = -size.width * (0.25f + drift * 0.5f), y = 0f),
            end = Offset(x = size.width * (1.25f - drift * 0.35f), y = size.height)
        )
    )

    drawRect(
        brush = Brush.radialGradient(
            colors = listOf(glow.copy(alpha = 0.20f), Color.Transparent),
            center = Offset(x = size.width * (0.15f + drift * 0.7f), y = size.height * 0.18f),
            radius = size.width * 0.75f
        )
    )
}
