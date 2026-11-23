package com.vigil.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.room.GoalEntity
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.GlassRow
import com.vigil.app.ui.components.SectionTitle
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilPalette
import com.vigil.app.ui.theme.VigilText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dayFormatter = DateTimeFormatter.ofPattern("d MMM")

@Composable
fun ProgressScreen(vm: VigilViewModel) {
    val stats by vm.stats.collectAsStateWithLifecycle()
    val checkIns by vm.checkIns.collectAsStateWithLifecycle()
    val goals by vm.goals.collectAsStateWithLifecycle()
    val today by vm.today.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 8.dp, 0.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle("Overview", "Locally stored, never uploaded") }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(Modifier.weight(1f), stats.streak.toString(), "current", VigilPalette.Warning)
                StatTile(Modifier.weight(1f), stats.best.toString(), "longest", VigilPalette.IcyCyan)
                StatTile(Modifier.weight(1f), stats.relapses.toString(), "setbacks", VigilPalette.Danger)
            }
        }

        item { SectionTitle("Last 30 days", "Filled = checked in") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    val days = checkIns.map {
                        Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    }.toSet()
                    val relapseDays = checkIns.filter { it.hadRelapse }.map {
                        Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    }.toSet()
                    val todayDate = java.time.LocalDate.now()
                    for (week in 0 until 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (d in 0 until 7) {
                                val offset = (4 - week) * 7 + (6 - d)
                                val date = todayDate.minusDays(offset.toLong())
                                val checked = days.contains(date)
                                val relapsed = relapseDays.contains(date)
                                val bg = when {
                                    relapsed -> VigilPalette.Danger.copy(alpha = 0.35f)
                                    checked -> VigilPalette.Success.copy(alpha = 0.35f)
                                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(bg)
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                    }
                    Text(
                        text = "green = check-in · red = setback",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SectionTitle("Check-in", "A setback is data, not a verdict") }

        item { CheckInCard(today?.mood ?: 0, today?.hadRelapse ?: false) { mood, notes, urge, relapse ->
            vm.saveCheckIn(mood, notes, urge, relapse)
        } }

        item { SectionTitle("Goals") }

        item { NewGoalRow { title, detail, days -> vm.createGoal(title, detail, days) } }

        if (goals.isEmpty()) {
            item {
                GlassRow {
                    Text(
                        "No active goals.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(goals) { goal -> GoalCard(goal, vm) }
        }

        item { SectionTitle("Recent check-ins") }

        if (checkIns.isEmpty()) {
            item {
                GlassRow {
                    Text(
                        "Nothing recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(checkIns.take(10)) { entry ->
                GlassRow {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = Instant.ofEpochMilli(entry.date)
                                    .atZone(ZoneId.systemDefault()).toLocalDate().format(dayFormatter),
                                style = MaterialTheme.typography.titleSmall
                            )
                            if (entry.notes.isNotBlank()) {
                                Text(
                                    entry.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = moodLabel(entry.mood),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (entry.hadRelapse) VigilPalette.Danger else VigilPalette.Success
                        )
                    }
                }
            }
        }
    }
}

private fun moodLabel(mood: Int): String = when (mood) {
    5 -> "great"
    4 -> "good"
    3 -> "okay"
    2 -> "shaky"
    1 -> "rough"
    else -> "—"
}

@Composable
private fun StatTile(modifier: Modifier, value: String, label: String, tint: Color) {
    GlassRow(modifier = modifier) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, style = VigilText.statNumber, color = tint)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckInCard(
    existingMood: Int,
    existingRelapse: Boolean,
    onSave: (Int, String, Boolean, Boolean) -> Unit
) {
    var mood by remember { mutableIntStateOf(if (existingMood == 0) 3 else existingMood) }
    var notes by remember { mutableStateOf("") }
    var urge by remember { mutableStateOf(false) }
    var relapse by remember { mutableStateOf(existingRelapse) }

    GlassRow {
        Column(Modifier.fillMaxWidth()) {
            Text("How is today?", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("rough" to 1, "shaky" to 2, "okay" to 3, "good" to 4, "great" to 5).forEach { (label, value) ->
                    Pill(
                        label = label,
                        selected = mood == value,
                        tint = VigilPalette.IcyCyan
                    ) { mood = value }
                }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("urge", urge, VigilPalette.Warning) { urge = !urge }
                Pill("setback", relapse, VigilPalette.Danger) { relapse = !relapse }
                Spacer(Modifier.weight(1f))
                ActionPill("Save") { onSave(mood, notes, urge, relapse); notes = "" }
            }
        }
    }
}

@Composable
private fun NewGoalRow(onCreate: (String, String, Int) -> Unit) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("30") }
    GlassRow {
        Column(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Goal") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detail,
                onValueChange = { detail = it },
                label = { Text("Why it matters") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter(Char::isDigit).take(4) },
                    label = { Text("Days") },
                    singleLine = true,
                    modifier = Modifier.width(96.dp)
                )
                Spacer(Modifier.weight(1f))
                ActionPill("Create") {
                    val target = days.toIntOrNull() ?: 30
                    if (title.isNotBlank() && target > 0) {
                        onCreate(title, detail, target)
                        title = ""
                        detail = ""
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: GoalEntity, vm: VigilViewModel) {
    GlassRow {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { vm.deleteGoal(goal) }) {
                    Icon(VigilIcons.delete, "Delete goal")
                }
            }
            if (goal.description.isNotBlank()) {
                Text(
                    goal.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${goal.targetDays} day target",
                style = MaterialTheme.typography.labelMedium,
                color = VigilPalette.IcyCyan
            )
        }
    }
}

@Composable
private fun Pill(
    label: String,
    selected: Boolean,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) tint.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActionPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(VigilPalette.IcyCyan.copy(alpha = 0.22f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = VigilPalette.IcyCyan
        )
    }
}


