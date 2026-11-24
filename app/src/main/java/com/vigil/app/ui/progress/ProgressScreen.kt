package com.vigil.app.ui.progress

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.elapsed
import com.vigil.app.data.milestones
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import com.vigil.app.ui.theme.VigilIcons
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ProgressScreen(vm: VigilViewModel) {
    val now by vm.clock.collectAsStateWithLifecycle()
    val runs by vm.runs.collectAsStateWithLifecycle()
    val entries by vm.entries.collectAsStateWithLifecycle()
    val goals by vm.goals.collectAsStateWithLifecycle()

    val today = Instant.ofEpochMilli(now).atZone(ZoneId.systemDefault()).toLocalDate()
    val active = runs.firstOrNull { it.end == null }?.elapsed(now)?.toDays() ?: 0L
    val best = runs.maxOfOrNull { it.elapsed(now).toDays() } ?: 0L
    val comebacks = (runs.size - 1).coerceAtLeast(0)

    var month by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    val m = YearMonth.parse(month)
    val daySet = remember(entries) { entries.map { it.day }.toSet() }
    val setbackSet = remember(entries) { entries.filter { it.setback }.map { it.day }.toSet() }
    var sheet by rememberSaveable { mutableStateOf("") }

    Page("Progress", "V I G I L    /    Your journal") {
        Kicker("Clean days")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = active.toString(),
                label = "now",
                accent = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = best.toString(),
                label = "best",
                accent = MaterialTheme.colorScheme.secondary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = comebacks.toString(),
                label = "comebacks",
                accent = MaterialTheme.colorScheme.tertiary
            )
        }

        Text(
            "$comebacks ${if (comebacks == 1) "return" else "returns"} · every one is practice, not a loss.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        m.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { month = m.minusMonths(1).toString() }) { Text("Prev") }
                    TextButton(
                        enabled = m < YearMonth.from(today),
                        onClick = { month = m.plusMonths(1).toString() }
                    ) { Text("Next") }
                }
                Spacer(Modifier.height(12.dp))
                Row {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                val offset = m.atDay(1).dayOfWeek.value - 1
                repeat((offset + m.lengthOfMonth() + 6) / 7) { w ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { d ->
                            val n = w * 7 + d - offset + 1
                            val valid = n in 1..m.lengthOfMonth()
                            val date = if (valid) m.atDay(n).toString() else ""
                            val clean = date.isNotEmpty() && daySet.contains(date) && !setbackSet.contains(date)
                            val setback = date.isNotEmpty() && setbackSet.contains(date)
                            val isToday = date == today.toString()
                            Box(
                                Modifier.weight(1f).padding(2.dp).aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            clean -> MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                                            setback -> MaterialTheme.colorScheme.error.copy(alpha = 0.28f)
                                            isToday -> MaterialTheme.colorScheme.surfaceContainerHighest
                                            else -> androidx.compose.ui.graphics.Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (valid) {
                                    Text(
                                        n.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (clean || setback || isToday)
                                            MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    LegendDot(MaterialTheme.colorScheme.primary.copy(alpha = 0.38f), "Clean day")
                    LegendDot(MaterialTheme.colorScheme.error.copy(alpha = 0.28f), "Setback")
                }
            }
        }

        val reached = milestones.filter { best >= it }
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(13.dp))
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(VigilIcons.trophy, null, tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(21.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Milestones", style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (reached.isEmpty()) "Your first marker is a full day."
                        else "${reached.size} reached · ${reached.joinToString { "$it" }} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        NavRow(VigilIcons.flag, "Personal goals",
            goals.firstOrNull { !it.complete }?.title ?: "Choose something worth making time for") {
            sheet = "Personal goals"
        }
        NavRow(VigilIcons.doc, "Journal history", "${daySet.size} days logged") { sheet = "Journal history" }
        NavRow(VigilIcons.trendingUp, "Streak history", "Milestones and past clean runs") { sheet = "Streak history" }
    }

    if (sheet.isNotEmpty()) Sheet(sheet, { sheet = "" }) {
        when (sheet) {
            "Personal goals" -> {
                var title by rememberSaveable { mutableStateOf("") }
                OutlinedTextField(title, { title = it.take(200) }, label = { Text("One intention") },
                    modifier = Modifier.fillMaxWidth())
                Button(enabled = title.isNotBlank(), onClick = { vm.goal(title); title = "" },
                    shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Add goal")
                }
                goals.forEach { g ->
                    ActionRow(g.title, if (g.complete) "Completed · tap to reopen" else "Active · tap to complete") {
                        vm.complete(g)
                    }
                }
            }

            "Journal history" -> {
                if (entries.isEmpty()) Text("Your outcomes will appear here after your first daily decision.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                var count by remember(sheet) { mutableIntStateOf(20) }
                entries.take(count).forEach { e ->
                    Kicker(e.day)
                    Text(e.note.ifBlank { if (e.setback) "A new start" else "Passed clean" })
                }
                if (count < entries.size) TextButton(onClick = { count += 20 }) { Text("Show more") }
            }

            else -> {
                Text("Earned milestones: " + reached.joinToString { "$it days" }.ifBlank { "None yet" })
                if (runs.isEmpty()) Text("Set your starting date on Home.")
                runs.forEach { r ->
                    Text("${dateLabel(r.start)} — ${r.end?.let(::dateLabel) ?: "today"}")
                    Text("${r.elapsed(now).toDays()} days · ${r.elapsed(now).toHours() % 24} hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, value: String, label: String, accent: androidx.compose.ui.graphics.Color) {
    Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = accent)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(detail, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(VigilIcons.chevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
