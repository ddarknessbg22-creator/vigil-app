package com.vigil.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/**
 * Living blue gradient, drawn entirely in the draw phase.
 *
 * The animated value is read ONLY inside `drawBehind` — Compose invalidates
 * the draw node, not the composition. No screen recomposes per frame.
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
    val phase = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        phase.animateTo(
            1f,
            infiniteRepeatable(tween(periodMs / 2, easing = LinearEasing), RepeatMode.Reverse)
        )
    }

    return this.drawBehind {
        // Read in draw phase only — never in composition.
        val p = phase.value
        val drift = ((sin(p.toDouble() * 2.0 * PI) + 1.0) / 2.0).toFloat()
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
