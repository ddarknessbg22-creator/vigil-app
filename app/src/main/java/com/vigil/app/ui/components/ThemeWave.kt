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
 * A one-shot expanding wave played when the theme changes.
 *
 * Everything happens in the draw phase: no recomposition per frame, so the
 * transition stays at display refresh rate.
 */
@Composable
fun Modifier.themeWave(themeKey: String): Modifier {
    val wave = remember { Animatable(0f) }
    val from = remember { mutableThemeKey(themeKey) }

    LaunchedEffect(themeKey) {
        if (from.value == themeKey) return@LaunchedEffect
        from.value = themeKey
        wave.snapTo(0f)
        wave.animateTo(1f, tween(750))
    }

    val active = wave.value > 0f && wave.value < 1f

    return this.then(
        Modifier.drawWithContent {
            drawContent()
            if (active) {
                val p = wave.value
                val eased = 1f - (1f - p) * (1f - p)
                val radius = hypot(size.width, size.height) * eased
                val center = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = Color(0xFF0A84FF).copy(alpha = (1f - p) * 0.28f),
                    radius = radius,
                    center = center
                )
                drawCircle(
                    color = Color(0xFF8CC4FF).copy(alpha = (1f - p) * 0.16f),
                    radius = radius * 0.82f,
                    center = center
                )
            }
        }
    )
}

private class MutableThemeKey(var value: String)
private fun mutableThemeKey(value: String) = MutableThemeKey(value)
