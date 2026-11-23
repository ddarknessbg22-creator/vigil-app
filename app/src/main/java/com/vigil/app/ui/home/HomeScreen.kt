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
import com.vigil.app.data.room.Entry
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.Page
import com.vigil.app.ui.components.QuoteCard
import com.vigil.app.ui.components.Sheet
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
    val all by vm.quotes.collectAsStateWithLifecycle()
    val o by vm.options.collectAsStateWithLifecycle()
    val q = remember(all, today, time.hour, o.category) {
        Quotes.featured(all, time.toLocalDateTime(), o.category)
    }
    LaunchedEffect(q?.id) { q?.let { vm.seen(it.id) } }

    var sheet by rememberSaveable { mutableStateOf(false) }
    val c = LocalContext.current

    Page("Today", "V I G I L   /   ${today.format(DateTimeFormatter.ofPattern("EEE, d MMM"))}") {
        StreakCard(
            run = run,
            elapsed = elapsed,
            entries = entries,
            today = today,
            best = runs.map { ((it.end ?: now) - it.start).coerceAtLeast(0) / 86_400_000 }.maxOrNull() ?: 0L,
            onSetStart = { pickStart(c, vm::start) }
        )

        q?.let { QuoteCard(it, if (time.hour in 5..17) "Morning note" else "Evening note") }

        Button(
            onClick = { if (run == null) pickStart(c, vm::start) else sheet = true },
            modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Text(
                if (run == null) "Start my streak"
                else if (check == null) "Check in for today"
                else "View today’s check-in",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }

    if (sheet) {
        var note by rememberSaveable { mutableStateOf(check?.note.orEmpty()) }
        Sheet("A moment for today", { sheet = false }) {
            Text(
                if (time.hour in 5..17) "What would you like to make room for today?"
                else "What helped you stay close to your intention?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                note, { note = it.take(4000) },
                label = { Text("Private reflection (optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { vm.reflect(note); sheet = false },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(26.dp)
            ) { Text(if (check == null) "Keep my commitment" else "Save reflection") }
        }
    }
}

/** A streak, not a status banner: dark surface, flame, heavy number, week dots. */
@Composable
private fun StreakCard(
    run: com.vigil.app.data.room.Run?,
    elapsed: Duration,
    entries: List<Entry>,
    today: LocalDate,
    best: Long,
    onSetStart: () -> Unit
) {
    val days = if (run == null) 0L else elapsed.toDays().coerceAtLeast(0)
    val shown by animateIntAsState(days.toInt(), tween(900), label = "streakNumber")
    val nextMilestone = listOf(7L, 14L, 30L, 60L, 90L, 180L, 365L)
        .firstOrNull { it > days } ?: 365L
    val progress = (days.toFloat() / nextMilestone).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, spring(stiffness = 140f), label = "streakProgress")

    var ready by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { ready = true }
    val appear by animateFloatAsState(if (ready) 1f else 0.94f, tween(420), label = "streakAppear")

    val dayKeys = remember(entries) { entries.map { it.day }.toSet() }
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
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔥", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "Current streak",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.height(30.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Best $best",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (run == null) "—" else shown.toString(),
                    style = VigilText.heroNumber.copy(
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                Color(0xFF7FC4FF)
                            )
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
                    run == null -> "Every streak starts somewhere. Choose your beginning."
                    days < 1 -> "Today is day one. It counts."
                    else -> "${elapsed.toHours() % 24} hours · ${elapsed.toMinutes() % 60} minutes into today"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEachIndexed { i, date ->
                    val checked = dayKeys.contains(date.toString())
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
                                        checked -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Next marker  ·  $nextMilestone days",
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    Color(0xFF7FC4FF)
                                )
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
