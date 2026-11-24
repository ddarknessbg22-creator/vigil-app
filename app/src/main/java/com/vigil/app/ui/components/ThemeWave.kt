package com.vigil.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.hypot

/**
 * A one-shot material-style reveal played when the theme changes.
 *
 * A dark veil sweeps in from the centre and fades out as the new theme settles.
 * Everything is drawn in the draw phase — no recomposition per frame.
 */
@Composable
fun Modifier.themeWave(themeKey: String): Modifier {
    val anim = remember { Animatable(0f) }
    val previous = remember { Array(1) { themeKey } }

    LaunchedEffect(themeKey) {
        if (previous[0] == themeKey) return@LaunchedEffect
        previous[0] = themeKey
        anim.snapTo(0f)
        anim.animateTo(1f, tween(900))
    }

    val active = anim.value > 0f && anim.value < 1f
    return this.then(
        Modifier.drawWithContent {
            drawContent()
            if (active) {
                val p = anim.value
                // Fast expansion, slow fade — like a curtain lifting.
                val radius = hypot(size.width, size.height) *
                    (1f - (1f - p) * (1f - p) * (1f - p))
                val alpha = (1f - p) * 0.35f
                drawCircle(
                    color = Color(0xFF0A84FF).copy(alpha = alpha),
                    radius = radius,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.25f),
                    radius = radius * 0.72f,
                    center = Offset(size.width / 2f, size.height / 2f)
                )
            }
        }
    )
}
