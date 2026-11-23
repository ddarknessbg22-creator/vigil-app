package com.vigil.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.vigil.app.ui.theme.LocalGlassEnabled
import com.vigil.app.ui.theme.LocalGlassTokens

/**
 * Layered "liquid glass" surface.
 *
 * Rendering budget: one gradient fill + one hairline border + one specular
 * highlight. No RenderEffect blur, so it stays cheap on mid-range hardware
 * and inside scrolling lists.
 *
 * Content is a [BoxScope], so do NOT call `Modifier.weight()` inside: weight
 * is a RowScope/ColumnScope modifier. Pass it in via [modifier] instead.
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
    val glassOn = LocalGlassEnabled.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && glassOn) 0.985f else 1f,
        animationSpec = spring(stiffness = 900f),
        label = "glassScale"
    )
    val tintTop = when {
        !glassOn -> MaterialTheme.colorScheme.surfaceContainerHigh
        strong -> tokens.surfaceTintStrong
        else -> tokens.surfaceTint
    }
    val tintBottom = tintTop.copy(alpha = if (glassOn) tintTop.alpha * 0.45f else 1f)
    val borderBrush = Brush.verticalGradient(listOf(tokens.borderTop, tokens.borderBottom))
    val fillBrush = Brush.verticalGradient(listOf(tintTop, tintBottom))
    val glowBrush = Brush.verticalGradient(0f to tokens.innerGlow, 0.08f to androidx.compose.ui.graphics.Color.Transparent)
    val shadow by animateFloatAsState(if (glassOn) 14f else 2f, label = "glassShadow")

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                shadowElevation = shadow
                this.shape = shape
                clip = false
            }
            .clip(shape)
            .background(fillBrush)
            .then(if (glassOn) Modifier.background(glowBrush) else Modifier)
            .then(if (glassOn) Modifier.border(1.dp, borderBrush, shape) else Modifier)
            .let { base ->
                if (onClick != null) {
                    base.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    base
                }
            }
            .padding(contentPadding),
        content = content
    )
}

/** Glass card used for list rows. Same scope rules as [GlassSurface]. */
@Composable
fun GlassRow(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val tokens = LocalGlassTokens.current
    val glassOn = LocalGlassEnabled.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (!glassOn) {
                    androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.surfaceContainerHigh)
                } else {
                    Brush.verticalGradient(
                        listOf(
                            tokens.surfaceTint.copy(alpha = tokens.surfaceTint.alpha * 0.85f),
                            tokens.surfaceTint.copy(alpha = 0.02f)
                        )
                    )
                }
            )
            .then(
                if (glassOn) {
                    Modifier.border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                tokens.borderTop.copy(alpha = 0.16f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        ),
                        shape
                    )
                } else {
                    Modifier
                }
            )
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        content = content
    )
}

/** Uppercase section label with an optional subtitle. */
@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Box(Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Column {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (subtitle != null) {
                Text(
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
