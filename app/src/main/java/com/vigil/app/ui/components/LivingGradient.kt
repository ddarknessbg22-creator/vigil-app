package com.vigil.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/**
 * Living blue gradient: a slow sweep of light across the card.
 *
 * The animated value is read inside `drawBehind`, so the animation stays in
 * the draw phase and never re-composes the screen.
 */
@Composable
fun Modifier.livingGradient(
    base: List<Color> = listOf(
        Color(0xFF0A1E3D),
        Color(0xFF103A76),
        Color(0xFF0A5FD0),
        Color(0xFF0A1E3D)
    ),
    glow: Color = Color(0xFF4C9DFF),
    periodMs: Int = 9000
): Modifier {
    val phase by rememberInfiniteTransition(label = "gradient").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )
    val drift = (sin(phase * 2f * PI) + 1f) / 2f

    return this.drawBehind {
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
}
