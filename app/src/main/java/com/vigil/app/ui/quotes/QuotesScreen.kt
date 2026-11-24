package com.vigil.app.ui.quotes

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.Quotes
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.Kicker
import com.vigil.app.ui.components.Page
import com.vigil.app.ui.theme.VigilIcons
import java.time.Instant
import java.time.ZoneId

/**
 * A focused reading room: one quote per page, swipe to move through today's
 * selection. No wall of cards, no filter chip clutter.
 */
@Composable
fun QuotesScreen(vm: VigilViewModel) {
    val all by vm.quotes.collectAsStateWithLifecycle()
    val reads by vm.readings.collectAsStateWithLifecycle()
    val o by vm.options.collectAsStateWithLifecycle()
    val now by vm.clock.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val time = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDateTime()

    // Today's pool is derived once per day, not on every recomposition.
    val dailyPool = remember(all, time.toLocalDate(), o.category) {
        val filtered = all.filter { o.category == "Any" || it.category == o.category }
        val preferred = Quotes.featured(filtered, time, o.category)
        val rest = filtered.filter { it.id != preferred?.id }.shuffled().take(11)
        listOfNotNull(preferred) + rest
    }

    val pagerState = rememberPagerState(pageCount = { dailyPool.size.coerceAtLeast(1) })
    var showInfo by rememberSaveable { mutableStateOf(false) }

    Page("Quotes", "VIGIL / Reading room") {
        Text(
            "A line for today. Swipe for more.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (dailyPool.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No quotes available.", modifier = Modifier.padding(20.dp))
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
            ) { page ->
                val q = dailyPool[page]
                LaunchedEffect(q.id) { vm.seen(q.id) }

                val isFocused = pagerState.currentPage == page
                val scale by animateFloatAsState(
                    if (isFocused) 1f else 0.94f,
                    tween(240),
                    label = "quoteScale"
                )
                val alpha by animateFloatAsState(
                    if (isFocused) 1f else 0.55f,
                    tween(240),
                    label = "quoteAlpha"
                )

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayerCompat(scale, alpha)
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Kicker(if (page == 0) "TODAY'S LINE" else "ANOTHER THOUGHT")
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${page + 1}/${dailyPool.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        Text(
                            "“${q.text}”",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.28f
                            ),
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            q.source.ifBlank { "VIGIL original" },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            q.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.weight(1f))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            val saved = reads.any { it.id == q.id && it.saved }
                            FilledPill(
                                text = if (saved) "Saved" else "Save",
                                icon = VigilIcons.heart,
                                tint = if (saved) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary,
                                filled = false,
                                modifier = Modifier.weight(1f)
                            ) { vm.favorite(q.id) }

                            FilledPill(
                                text = "Share",
                                icon = VigilIcons.upload,
                                tint = MaterialTheme.colorScheme.primary,
                                filled = false,
                                modifier = Modifier.weight(1f)
                            ) {
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).setType("text/plain")
                                            .putExtra(Intent.EXTRA_TEXT, "“${q.text}” — ${q.source.ifBlank { "VIGIL original" }}"),
                                        "Share quote"
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(dailyPool.size.coerceAtMost(12)) { index ->
                    val active = pagerState.currentPage == index
                    val width by animateDpAsStateCompat(if (active) 18.dp else 6.dp)
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .width(width)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                    )
                }
            }

            TextButton(
                onClick = { showInfo = !showInfo },
                modifier = Modifier.align(Alignment.End)
            ) { Text(if (showInfo) "Hide details" else "About these quotes") }

            AnimatedVisibility(
                visible = showInfo,
                enter = fadeIn(tween(180)) + expandVertically(tween(220)),
                exit = fadeOut(tween(140)) + shrinkVertically(tween(180))
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Most lines here are original VIGIL writing. Attributed classics and anime-inspired lines carry their source on the card. The theme choice lives in Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun animateFloatAsStateCompat(target: Float, label: String) =
    androidx.compose.animation.animateFloatAsState(target, tween(240), label)

@Composable
private fun animateDpAsStateCompat(target: androidx.compose.ui.unit.Dp) =
    androidx.compose.animation.animateDpAsState(target, tween(220), label = "dotWidth")

@Composable
private fun Modifier.graphicsLayerCompat(scale: Float, alpha: Float): Modifier =
    this.then(Modifier.graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha })

@Composable
private fun FilledPill(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    filled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) tint else tint.copy(alpha = 0.14f),
            contentColor = if (filled) Color.White else tint
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
