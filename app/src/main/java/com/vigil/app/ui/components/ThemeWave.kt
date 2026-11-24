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
 * Theme-change reveal, drawn entirely in the draw phase.
 * No value is read in composition, so the Scaffold never recomposes per frame.
 */
@Composable
fun Modifier.themeWave(themeKey: String): Modifier {
    val anim = remember { Animatable(0f) }
    val key = remember { Array(1) { themeKey } }

    LaunchedEffect(themeKey) {
        if (key[0] == themeKey) return@LaunchedEffect
        key[0] = themeKey
        anim.snapTo(0f)
        anim.animateTo(1f, tween(900))
    }

    return this.then(
        Modifier.drawWithContent {
            drawContent()
            val p = anim.value
            if (p > 0f && p < 1f) {
                val radius = hypot(size.width, size.height) *
                    (1f - (1f - p) * (1f - p) * (1f - p))
                val alpha = (1f - p) * 0.28f
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(Color(0xFF0A84FF).copy(alpha = alpha), radius, center)
                drawCircle(Color.White.copy(alpha = alpha * 0.22f), radius * 0.72f, center)
            }
        }
    )
}
