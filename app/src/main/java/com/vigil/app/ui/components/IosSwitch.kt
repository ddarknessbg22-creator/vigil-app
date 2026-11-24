package com.vigil.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** iOS-style switch: the thumb follows your finger and springs into place. */
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
    val trackW = 51.dp
    val trackH = 31.dp
    val thumbD = 27.dp
    val inset = 2.dp

    val progress = remember { Animatable(if (checked) 1f else 0f) }
    LaunchedEffect(checked) {
        progress.animateTo(if (checked) 1f else 0f, spring(dampingRatio = 0.86f, stiffness = 420f))
    }

    Box(
        modifier = modifier
            .width(trackW)
            .height(trackH)
            .clip(RoundedCornerShape(50))
            .background(if (checked) activeColor else Color(0xFF39393D))
            .pointerInput(enabled) {
                val thumbPx = with(density) { thumbD.toPx() }
                val insetPx = with(density) { inset.toPx() }
                val travel = (size.width - thumbPx - insetPx * 2).coerceAtLeast(1f)

                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dx ->
                        launch { progress.snapTo((progress.value + dx / travel).coerceIn(0f, 1f)) }
                    },
                    onDragEnd = {
                        val on = progress.value >= 0.5f
                        launch {
                            progress.animateTo(
                                if (on) 1f else 0f,
                                spring(dampingRatio = 0.82f, stiffness = 380f)
                            )
                        }
                        if (on != checked) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onCheckedChange(on)
                        }
                    }
                )
                detectTapGestures {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(!checked)
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
