package com.vigil.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vigil.app.ui.components.GlassRow
import com.vigil.app.ui.components.SectionTitle
import com.vigil.app.ui.theme.VigilPalette

@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar(onBack = onNavigateBack, title = "Progress") }
        item { HeroStatsCard() }
        item { ProgressCalendarCard() }
        item { MilestonesCard() }
        item { WeeklySummaryCard() }
        item { MonthlySummaryCard() }
        item { GoalsCard() }
        item { DailyCheckInCard() }
    }
}

@Composable
fun TopBar(
    onBack: () -> Unit,
    title: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun HeroStatsCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Your Journey", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatColumn("Current Streak", "47", "days", VigilPalette.Warning, androidx.compose.material.icons.Icons.Default.LocalFireDepartment)
                DividerVert()
                StatColumn("Longest Streak", "89", "days", VigilPalette.IcyCyan, androidx.compose.material.icons.Icons.Default.EmojiEvents)
                DividerVert()
                StatColumn("Total Days", "342", "tracked", VigilPalette.Success, androidx.compose.material.icons.Icons.Default.CalendarToday)
                DividerVert()
                StatColumn("Check-ins", "298", "completed", VigilPalette.ElectricBlue, androidx.compose.material.icons.Icons.Default.CheckCircle)
            }
        }
    }
}

@Composable
fun StatColumn(
    label: String,
    value: String,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = "", tint = color, modifier = Modifier.size(22.dp))
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.statNumber, color = MaterialTheme.colorScheme.onSurface)
        Text(text = unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = androidx.compose.ui.text.TextAlign.Center)
    }
}

@Composable
fun DividerVert() {
    androidx.compose.foundation.layout.VerticalDivider(
        modifier = Modifier.fillMaxHeight().width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}

@Composable
fun ProgressCalendarCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Activity Calendar", "Your daily check-ins and progress", androidx.compose.material.icons.Icons.Default.CalendarMonth, VigilPalette.IcyCyan)
            Text(text = "Calendar grid renders here", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MilestonesCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Milestones", "Achievements unlocked", androidx.compose.material.icons.Icons.Default.EmojiEvents, VigilPalette.Warning)
            MilestoneItem("First Week", "Completed 7 days of tracking", androidx.compose.material.icons.Icons.Default.Star, VigilPalette.Warning, true, "Jan 15, 2024")
            MilestoneItem("Month Warrior", "30 consecutive days protected", androidx.compose.material.icons.Icons.Default.Shield, VigilPalette.IcyCyan, true, "Feb 14, 2024")
            MilestoneItem("Quarter Master", "90 days of discipline", androidx.compose.material.icons.Icons.Default.EmojiEvents, VigilPalette.ElectricBlue, true, "May 15, 2024")
            MilestoneItem("Half Year Hero", "180 days on the path", androidx.compose.material.icons.Icons.Default.MilitaryTech, VigilPalette.Success, false, "Day 47/180")
        }
    }
}

@Composable
fun MilestoneItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    achieved: Boolean,
    date: String
) {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = "", tint = if (achieved) color else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                if (achieved) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle, contentDescription = "Achieved", tint = color, modifier = Modifier.size(22.dp))
                    Text(text = date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(text = date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun WeeklySummaryCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Weekly Summary", "Last 7 days", androidx.compose.material.icons.Icons.Default.TrendingUp, VigilPalette.Success)
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeeklyStat("Check-ins", "6/7", VigilPalette.Success)
                WeeklyStat("Avg. Mood", "3.8/5", VigilPalette.IcyCyan)
                WeeklyStat("Urges", "3", VigilPalette.Warning)
                WeeklyStat("Relapses", "0", VigilPalette.Danger)
            }
        }
    }
}

@Composable
fun WeeklyStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MonthlySummaryCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Monthly Summary", "Last 30 days", androidx.compose.material.icons.Icons.Default.Assessment, VigilPalette.ElectricBlue)
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonthlyStat("Days Tracked", "28/30", VigilPalette.Success)
                MonthlyStat("Check-ins", "24", VigilPalette.IcyCyan)
                MonthlyStat("Streak", "14 days", VigilPalette.Warning)
                MonthlyStat("Relapses", "1", VigilPalette.Danger)
            }
        }
    }
}

@Composable
fun MonthlyStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineMedium, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun GoalsCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Active Goals", "Your current commitments", androidx.compose.material.icons.Icons.Default.Flag, VigilPalette.ElectricBlue)
            GoalItem("30 Days Clean", "No adult content for 30 days", 0.65f, 10, VigilPalette.IcyCyan)
            GoalItem("Daily Check-in Streak", "Check in every day for 14 days", 0.85f, 2, VigilPalette.Success)
            GoalItem("Reduce Screen Time", "Limit social media to 30 min/day", 0.40f, 20, VigilPalette.Warning)
        }
    }
}

@Composable
fun GoalItem(
    title: String,
    description: String,
    progress: Float,
    daysRemaining: Int,
    color: androidx.compose.ui.graphics.Color
) {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "$daysRemaining days left", style = MaterialTheme.typography.labelMedium, color = color)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = progress,
                color = color,
                strokeWidth = 8.dp
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${(progress * 100).toInt()}% complete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DailyCheckInCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.EditNote, contentDescription = "", tint = VigilPalette.Success)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Daily Check-in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Reflect on your day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
