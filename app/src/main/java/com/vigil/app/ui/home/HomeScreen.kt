package com.vigil.app.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.Quotes
import com.vigil.app.data.elapsed
import com.vigil.app.data.room.Entry
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.Page
import com.vigil.app.ui.components.QuoteCard
import com.vigil.app.ui.components.Sheet
import com.vigil.app.ui.components.pickStart
import com.vigil.app.ui.theme.VigilText
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(vm: VigilViewModel) {
    val now by vm.clock.collectAsStateWithLifecycle()
    val time = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault())
    val today = time.toLocalDate()
    val runs by vm.runs.collectAsStateWithLifecycle()
    val run = runs.firstOrNull { it.end == null }
    val elapsed = run?.elapsed(now) ?: Duration.ZERO

    val entries by vm.entries.collectAsStateWithLifecycle()
    val check = remember(entries, today) { entries.firstOrNull { it.day == today.toString() } }
    val best = runs.map { ((it.end ?: now) - it.start).coerceAtLeast(0) / 86_400_000 }.maxOrNull() ?: 0L
    val nextTarget = listOf(7L, 14L, 30L, 60L, 90L, 180L, 365L)
        .firstOrNull { it > best } ?: 365L
    val all by vm.quotes.collectAsStateWithLifecycle()
    val o by vm.options.collectAsStateWithLifecycle()
    val q = remember(all, today, time.hour, o.category) {
        Quotes.featured(all, time.toLocalDateTime(), o.category)
    }
    LaunchedEffect(q?.id) { q?.let { vm.seen(it.id) } }

    var deciding by rememberSaveable { mutableStateOf<Boolean?>(null) }
    val c = LocalContext.current

    Page("Today", "V I G I L   /   Days clean · ${today.format(DateTimeFormatter.ofPattern("EEE, d MMM"))}") {
        Text(
            "One day at a time. No pornography, no gooning — just today.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CleanCard(
            run = run,
            elapsed = elapsed,
            entries = entries,
            today = today,
            best = best,
            onSetStart = { pickStart(c, vm::start) }
        )

        TodayDecision(check) { deciding = it }

        q?.let { QuoteCard(it, if (time.hour in 5..17) "Morning note" else "Evening note") }
    }

    deciding?.let { clean ->
        var note by rememberSaveable { mutableStateOf(check?.note.orEmpty()) }
        Sheet(if (clean) "I passed today" else "I had a setback", { deciding = null }) {
            Text(
                if (clean) "You stayed clean today. Add a thought if you want to remember it."
                else "You reached ${elapsed.toDays().coerceAtLeast(0)} days last time. That time is real and it stays yours. Your best is $best days — next marker $nextTarget. Begin again from now.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                note, { note = it.take(4000) },
                label = { Text("Private note (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    vm.checkIn(
                        clean,
                        note,
                        if (clean) "Today is logged clean."
                        else "New start. Best $best days still yours — next marker $nextTarget."
                    )
                    deciding = null
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (clean) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    if (clean) "Save — I passed today" else "Save — begin again",
                    color = Color.White
                )
            }
        }
    }
}

/** A clean-days counter, not a generic streak: purpose is in the surface itself. */
@Composable
private fun CleanCard(
    run: com.vigil.app.data.room.Run?,
    elapsed: Duration,
    entries: List<Entry>,
    today: LocalDate,
    best: Long,
    onSetStart: () -> Unit
) {
    val days = if (run == null) 0L else elapsed.toDays().coerceAtLeast(0)
    val shown by animateIntAsState(days.toInt(), tween(900), label = "cleanDays")
    val nextMilestone = listOf(7L, 14L, 30L, 60L, 90L, 180L, 365L)
        .firstOrNull { it > days } ?: 365L
    val progress = (days.toFloat() / nextMilestone).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, spring(stiffness = 140f), label = "cleanProgress")

    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ready = true }
    val appear by animateFloatAsState(if (ready) 1f else 0.94f, tween(420), label = "cleanAppear")

    val dayKeys = remember(entries) { entries.filter { !it.setback }.map { it.day }.toSet() }
    val week = remember(today) { (6 downTo 0).map { today.minusDays(it.toLong()) } }
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().scale(appear)
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Days clean",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "without pornography or gooning",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Text(
                        "Best $best",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (run == null) "—" else shown.toString(),
                    style = VigilText.heroNumber.copy(
                        brush = Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, Color(0xFF7FC4FF))
                        )
                    )
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (days == 1L) "day" else "days",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            Text(
                text = when {
                    run == null -> "Every clean run starts somewhere. Choose your beginning."
                    days < 1 -> "Today is day one. It counts."
                    else -> "${elapsed.toHours() % 24} hours · ${elapsed.toMinutes() % 60} minutes clean today"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { date ->
                    val marked = dayKeys.contains(date.toString())
                    val isToday = date == today
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            labels[(date.dayOfWeek.value - 1) % 7],
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        marked -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Next marker · $nextMilestone days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(animatedProgress).height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, Color(0xFF7FC4FF))
                            )
                        )
                )
            }

            if (run == null) {
                Spacer(Modifier.height(14.dp))
                TextButton(onClick = onSetStart, modifier = Modifier.align(Alignment.End)) {
                    Text("Set my start date")
                }
            }
        }
    }
}

@Composable
private fun TodayDecision(check: Entry?, onDecide: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(
                "Today’s decision",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    check == null -> "Be honest with yourself. Either answer moves you forward."
                    check.setback -> "Logged as a setback. You can change it if that was wrong."
                    else -> "Logged clean. You can change it if that was wrong."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onDecide(true) },
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) { Text("I passed today", style = MaterialTheme.typography.labelLarge) }

                OutlinedButton(
                    onClick = { onDecide(false) },
                    modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        )
                    )
                ) { Text("I had a setback", style = MaterialTheme.typography.labelLarge) }
            }
            Text(
                "A setback is a new start, not a judgement.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}
