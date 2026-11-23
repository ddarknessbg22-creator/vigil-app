package com.vigil.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.material3.MaterialTheme
import com.vigil.app.ui.theme.VigilPalette

/**
 * Ambient backdrop: base gradient plus two soft radial lights.
 * Drawn once per frame at the root, so it never sits inside a scrolling list.
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val cyanAlpha = if (dark) 0.16f else 0.12f
    val blueAlpha = if (dark) 0.14f else 0.09f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        scheme.background,
                        scheme.surface,
                        scheme.background
                    )
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        VigilPalette.IcyCyan.copy(alpha = cyanAlpha),
                        Color.Transparent
                    ),
                    center = Offset(140f, -60f),
                    radius = 900f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        VigilPalette.ElectricBlue.copy(alpha = blueAlpha),
                        Color.Transparent
                    ),
                    center = Offset(900f, 1600f),
                    radius = 1000f
                )
            ),
        content = content
    )
}
