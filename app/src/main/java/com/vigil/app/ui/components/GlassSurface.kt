package com.vigil.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vigil.app.ui.theme.LocalGlassTokens

/**
 * Layered "liquid glass" surface.
 *
 * Rendering budget: one gradient fill + one hairline border + one specular
 * highlight, all drawn into the same layer. No blur, no RenderEffect, so it
 * stays cheap on mid-range hardware and inside scrolling lists.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    strong: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "glassScale"
    )

    val tintTop = if (strong) tokens.surfaceTintStrong else tokens.surfaceTint
    val tintBottom = tintTop.copy(alpha = tintTop.alpha * 0.45f)
    val surfaceShape = shape

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = 18f
                this.shape = surfaceShape
                clip = false
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(tintTop, tintBottom),
                    startY = 0f,
                    endY = 600f
                )
            )
            // top-edge specular highlight
            .background(
                Brush.verticalGradient(
                    0f to tokens.innerGlow,
                    0.06f to Color.Transparent
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(tokens.borderTop, tokens.borderBottom)
                ),
                shape = shape
            )
            .let { base ->
                if (onClick != null) {
                    base.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                } else base
            }
    ) {
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            content()
        }
    }
}

/** Glass card sized to its content (used for list rows). */
@Composable
fun GlassRow(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(tokens.surfaceTint.copy(alpha = tokens.surfaceTint.alpha * 0.8f), tokens.surfaceTint.copy(alpha = 0.02f))
                )
            )
            .border(1.dp, Brush.verticalGradient(listOf(tokens.borderTop.copy(alpha = 0.16f), Color.Transparent)), shape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        content = content
    )
}

/** Section title used on every screen. */
@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Box(Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column {
            androidx.compose.material3.Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                androidx.compose.material3.Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        if (trailing != null) {
            Box(Modifier.align(androidx.compose.ui.Alignment.TopEnd)) { trailing() }
        }
    }
}

/** Ambient background: two soft radial lights over the base colour. */
@Composable
fun AmbientBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.background.luminanceIsDark()
    val cyan = com.vigil.app.ui.theme.VigilPalette.IcyCyan
    val blue = com.vigil.app.ui.theme.VigilPalette.ElectricBlue

    Box(
        modifier
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
                        cyan.copy(alpha = if (isDark) 0.16f else 0.13f),
                        Color.Transparent
                    ),
                    center = Offset(120f, -80f),
                    radius = 900f
                )
            )
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        blue.copy(alpha = if (isDark) 0.14f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(950f, 1500f),
                    radius = 1000f
                )
            ),
        content = content
    )
}

private fun Color.luminanceIsDark(): Boolean =
    (0.299f * red + 0.587f * green + 0.114f * blue) < 0.5f

/** Accessible hairline divider. */
@Composable
fun GlassDivider(modifier: Modifier = Modifier, padding: Dp = 0.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), Color.Transparent)
                )
            )
            .fillMaxWidth()
            .padding(top = 1.dp)
    )
}

@Composable
fun glassBorderBrush(): BorderStroke = BorderStroke(
    1.dp,
    Brush.verticalGradient(listOf(LocalGlassTokens.current.borderTop, LocalGlassTokens.current.borderBottom))
)
