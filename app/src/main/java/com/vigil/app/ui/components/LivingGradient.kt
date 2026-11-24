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
 * The animated value is read inside `drawBehind`, so it never re-composes.
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
    val drift = ((sin(phase.toDouble() * 2.0 * PI) + 1.0) / 2.0).toFloat()

    return this.drawBehind {
        val w = size.width
        val h = size.height
        drawRect(
            brush = Brush.linearGradient(
                colors = base,
                start = androidx.compose.ui.geometry.Offset(-w * (0.25f + drift * 0.5f), 0f),
                end = androidx.compose.ui.geometry.Offset(w * (1.25f - drift * 0.35f), h)
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = 0.20f), Color.Transparent),
                center = androidx.compose.ui.geometry.Offset(w * (0.15f + drift * 0.7f), h * 0.18f),
                radius = w * 0.75f
            )
        )
    }
}
