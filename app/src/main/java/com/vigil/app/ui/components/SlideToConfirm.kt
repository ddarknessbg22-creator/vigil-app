package com.vigil.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * iOS-style slide to confirm: drag the round thumb all the way right.
 * The fill follows the thumb so the action always feels responsive.
 */
@Composable
fun SlideToConfirm(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val progress = remember { Animatable(0f) }
    val height = 54.dp
    val thumb = 46.dp
    val pad = 4.dp
    val trackShape = RoundedCornerShape(27.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(trackShape)
            .background(color.copy(alpha = 0.16f))
            .pointerInput(Unit) {
                val thumbPx = with(density) { thumb.toPx() }
                val padPx = with(density) { pad.toPx() }
                val travel = (size.width - thumbPx - padPx * 2).coerceAtLeast(1f)

                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dx ->
                        scope.launch {
                            progress.snapTo((progress.value + dx / travel).coerceIn(0f, 1f))
                        }
                    },
                    onDragEnd = {
                        if (progress.value >= 0.9f) {
                            scope.launch { progress.animateTo(1f, spring(stiffness = 380f)) }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirm()
                        } else {
                            scope.launch { progress.animateTo(0f, spring(dampingRatio = 0.7f, stiffness = 300f)) }
                        }
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val thumbPx = with(density) { thumb.toPx() }
        val padPx = with(density) { pad.toPx() }
        val travel = with(density) { (height - thumb).toPx() }

        Box(
            Modifier
                .fillMaxWidth(progress.value.coerceIn(0.01f, 1f))
                .fillMaxSize()
                .background(color.copy(alpha = 0.30f))
        )

        Text(
            text,
            color = color,
            style = androidx.compose.material3.MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 64.dp),
            maxLines = 1
        )

        Box(
            Modifier
                .offset {
                    IntOffset(
                        (padPx + travel * progress.value).roundToInt(),
                        padPx.roundToInt()
                    )
                }
                .size(thumb)
                .clip(RoundedCornerShape(23.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}
