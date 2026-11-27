package com.vigil.app.ui.progress

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.domain.StreakVoice
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import com.vigil.app.data.elapsed
import com.vigil.app.data.cleanStreak
import com.vigil.app.data.bestStreak
import androidx.compose.ui.graphics.Color
import com.vigil.app.ui.theme.VigilIcons
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Progress = honest history, nothing else.
 * No goals, no milestone lists — just the counter and your own record.
 */
@Composable
fun ProgressScreen(vm: VigilViewModel) {
    val now by vm.clock.collectAsStateWithLifecycle()
    val runs by vm.runs.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()

    val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
    val activeRun = runs.firstOrNull { it.end == null }
    val active = cleanStreak(entries, today)
    val best = bestStreak(entries)
    val comebacks = (runs.size - 1).coerceAtLeast(0)

    Page("Progress", "V I G I L    /    Your record") {
        // Hero: current vs best
        Card {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CardLabel("THE LONG VIEW")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            active.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("right now", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            best.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text("personal best", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    StreakVoice.secondLine(active, today.toEpochDay()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$comebacks ${if (comebacks == 1) "return" else "returns"} · every one is practice.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar
        CalendarCard(now, entries, today)

        // Recent days
        CardLabel("RECENT DAYS")
        Card {
            if (entries.isEmpty()) {
                Text(
                    "Your daily decisions will appear here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp)
                )
            } else {
                entries.take(12).forEach { entry ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            runCatching { LocalDate.parse(entry.day) }
                                .getOrNull()?.format(DateTimeFormatter.ofPattern("EEE d MMM"))
                                ?: entry.day,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (entry.setback) {
                            Text("new start", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("passed", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarCard(now: Long, entries: List<com.vigil.app.data.room.Entry>, today: LocalDate) {
    var monthKey by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    val month = YearMonth.parse(monthKey)
    val clean = remember(entries) { entries.filter { !it.setback }.map { it.day }.toSet() }
    val setbacks = remember(entries) { entries.filter { it.setback }.map { it.day }.toSet() }

    Card {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { monthKey = month.minusMonths(1).toString() }) {
                    androidx.compose.material3.Icon(VigilIcons.back, null, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    enabled = month < YearMonth.from(today),
                    onClick = { monthKey = month.plusMonths(1).toString() }
                ) {
                    androidx.compose.material3.Icon(VigilIcons.chevronRight, null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(6.dp))
            val offset = month.atDay(1).dayOfWeek.value - 1
            repeat((offset + month.lengthOfMonth() + 6) / 7) { w ->
                Row {
                    repeat(7) { d ->
                        val n = w * 7 + d - offset + 1
                        val valid = n in 1..month.lengthOfMonth()
                        val date = if (valid) month.atDay(n).toString() else ""
                        val isClean = clean.contains(date)
                        val isSetback = setbacks.contains(date)
                        val isToday = date == today.toString()
                        val isFuture = valid && month.atDay(n).isAfter(today)
                        Box(
                            Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isClean -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                                        isSetback -> MaterialTheme.colorScheme.error.copy(alpha = 0.32f)
                                        isToday -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        isFuture -> Color.Transparent
                                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (valid) {
                                Text(n.toString(), style = MaterialTheme.typography.labelMedium,
                                    color = if (isClean || isSetback || isToday)
                                        MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Legend(MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), "clean")
                Legend(MaterialTheme.colorScheme.error.copy(alpha = 0.32f), "new start")
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(9.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
