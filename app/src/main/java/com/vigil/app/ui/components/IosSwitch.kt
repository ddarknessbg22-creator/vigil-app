package com.vigil.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * iOS-style switch.
 *
 * The drag owns the thumb while it happens and only the parent's new value
 * animates it afterwards — no competing LaunchedEffect, no jumping back.
 */
@Composable
fun IosSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = Color(0xFF34C759)
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val trackW = 51.dp
    val trackH = 31.dp
    val thumbD = 27.dp
    val inset = 2.dp

    val progress = remember { Animatable(if (checked) 1f else 0f) }
    val trackColor by animateColorAsState(
        if (checked) activeColor else Color(0xFF39393D),
        tween(180), label = "track"
    )

    Box(
        modifier = modifier
            .width(trackW)
            .height(trackH)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .pointerInput(enabled) {
                val thumbPx = with(density) { thumbD.toPx() }
                val insetPx = with(density) { inset.toPx() }
                val travel = (size.width - thumbPx - insetPx * 2).coerceAtLeast(1f)

                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dx ->
                        val next = (progress.value + dx / travel).coerceIn(0f, 1f)
                        scope.launch { progress.snapTo(next) }
                    },
                    onDragEnd = {
                        val next = progress.value >= 0.5f
                        scope.launch {
                            progress.animateTo(
                                if (next) 1f else 0f,
                                spring(dampingRatio = 0.86f, stiffness = 420f)
                            )
                        }
                        if (next != checked) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCheckedChange(next)
                        }
                    }
                )

                detectTapGestures {
                    val next = !checked
                    scope.launch {
                        progress.animateTo(
                            if (next) 1f else 0f,
                            spring(dampingRatio = 0.86f, stiffness = 420f)
                        )
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(next)
                }
            }
    ) {
        val thumbPx = with(density) { thumbD.toPx() }
        val insetPx = with(density) { inset.toPx() }
        val travel = with(density) { trackW.toPx() } - thumbPx - insetPx * 2
        Box(
            Modifier
                .offset {
                    IntOffset(
                        (insetPx + travel * progress.value).roundToInt(),
                        insetPx.roundToInt()
                    )
                }
                .width(thumbD)
                .height(thumbD)
                .clip(RoundedCornerShape(50))
                .background(Color.White)
        )
    }
}
