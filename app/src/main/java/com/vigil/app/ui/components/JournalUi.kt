package com.vigil.app.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.vigil.app.data.Quote
import java.time.*
import java.time.format.DateTimeFormatter

/**
 * Large iOS-style title that collapses into a hairline toolbar.
 *
 * The collapsed label is only placed in the top bar once the large title has
 * actually scrolled away; `graphicsLayer` writes are kept in the draw phase,
 * so scrolling never re-composes the page.
 */
@Composable
fun Page(
    title: String,
    kicker: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val collapseAt = with(density) { 44.dp.toPx() }
    val range = with(density) { 52.dp.toPx() }
    val collapse = ((scroll.value - collapseAt) / range).coerceIn(0f, 1f)

    // One-shot entrance, applied once per composition rather than per frame.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val entrance by animateFloatAsStateCompat(if (shown) 1f else 0.96f)

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = entrance }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(40.dp))
            Column(
                Modifier.graphicsLayer {
                    alpha = 1f - collapse
                    translationY = -collapse * 18f
                },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    kicker,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-1.2).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            content()
            Spacer(Modifier.height(32.dp))
        }

        // Collapsed bar: no ghosting, same colour as the cards beneath it.
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .graphicsLayer { alpha = collapse }
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.94f)),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
            Text(
                title,
                Modifier.padding(start = 20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun animateFloatAsStateCompat(target: Float) =
    androidx.compose.animation.animateFloatAsState(
        target,
        animationSpec = tween(320),
        label = "pageEntrance"
    )

@Composable
fun Kicker(s: String) {
    Text(s, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
fun QuoteCard(q: Quote, label: String) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Kicker(label)
            Text(
                "“${q.text}”",
                fontFamily = FontFamily.Default,
                fontSize = 23.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${q.source.ifBlank { "VIGIL original" }}  ·  ${q.category}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ActionRow(title: String, detail: String = "", action: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Surface(
        onClick = action,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (detail.isNotBlank()) {
                    Text(
                        detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

/** iOS-style centered sheet with a grabber, a single Close affordance and animated entry. */
@Composable
fun Sheet(title: String, close: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Dialog(onDismissRequest = close) {
        Surface(
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 24.dp,
            modifier = Modifier.fillMaxWidth(0.92f)
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 38.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline)
                )
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 6.dp))
                content()
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

fun pickStart(c: Context, save: (Long) -> Unit) {
    val now = ZonedDateTime.now()
    DatePickerDialog(
        c,
        { _, y, m, d ->
            TimePickerDialog(
                c,
                { _, h, min ->
                    val at = LocalDateTime.of(y, m + 1, d, h, min)
                        .atZone(now.zone).toInstant().toEpochMilli()
                    save(at)
                },
                now.hour, now.minute, true
            ).show()
        },
        now.year, now.monthValue - 1, now.dayOfMonth
    ).apply { datePicker.maxDate = System.currentTimeMillis(); show() }
}

fun dateLabel(t: Long): String = Instant.ofEpochMilli(t)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
