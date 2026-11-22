package com.vigil.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.VerticalDivider
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.vigil.app.ui.components.GlassRow
import com.vigil.app.ui.components.GlassSurface
import com.vigil.app.ui.components.SectionTitle
import com.vigil.app.ui.theme.VigilPalette

@Composable
fun HomeScreen(
    onNavigateToProtection: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 20.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProtectionStatusCard(onTapProtection = onNavigateToProtection) }
        item { CurrentGoalCard() }
        item { QuickStatsRow() }
        item { QuickActionsSection() }
        item { BlockingStatusCard() }
    }
}

@Composable
fun ProtectionStatusCard(
    onTapProtection: () -> Unit
) {
    val isProtected = true
    val daysProtected = 47
    val blockedToday = 12

    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        strong = true,
        onClick = onTapProtection
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isProtected) androidx.compose.material.icons.Icons.Default.Shield else androidx.compose.material.icons.Icons.Default.ShieldOff,
                    contentDescription = if (isProtected) "Protection Active" else "Protection Inactive",
                    tint = if (isProtected) VigilPalette.Success else VigilPalette.Danger,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isProtected) "Protection Active" else "Protection Inactive",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Discipline is a decision.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatItem(value = daysProtected, label = "Days Protected", icon = androidx.compose.material.icons.Icons.Default.CalendarToday)
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().width(1.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                StatItem(value = blockedToday, label = "Blocked Today", icon = androidx.compose.material.icons.Icons.Default.Block)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tap to configure protection →",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun StatItem(
    value: Int,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CurrentGoalCard() {
    val hasGoal = true
    val goalTitle = "30 Days Clean"
    val goalProgress = 0.65f
    val daysRemaining = 10

    GlassRow(
        modifier = Modifier.fillMaxWidth(),
        onClick = { }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Goal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (hasGoal) {
                    Text(
                        text = "$daysRemaining days left",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (hasGoal) {
                Text(
                    text = goalTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = goalProgress,
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(goalProgress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "No goal set. Create one in Progress.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard("This Week", "6", "check-ins", androidx.compose.material.icons.Icons.Default.CheckCircle, VigilPalette.Success)
        QuickStatCard("Streak", "47", "days", androidx.compose.material.icons.Icons.Default.LocalFireDepartment, VigilPalette.Warning)
        QuickStatCard("Blocked", "234", "total", androidx.compose.material.icons.Icons.Default.Security, VigilPalette.IcyCyan)
    }
}

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    GlassRow(
        modifier = Modifier.weight(1f).height(100.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.statNumber,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickActionsSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    icon = androidx.compose.material.icons.Icons.Default.Science,
                    label = "Test Blocking",
                    color = VigilPalette.IcyCyan
                )
                QuickActionButton(
                    icon = androidx.compose.material.icons.Icons.Default.EditNote,
                    label = "Daily Check-in",
                    color = VigilPalette.Success
                )
                QuickActionButton(
                    icon = androidx.compose.material.icons.Icons.Default.TrendingUp,
                    label = "View Progress",
                    color = VigilPalette.ElectricBlue
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    GlassRow(
        modifier = Modifier.weight(1f).height(80.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun BlockingStatusCard() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Blocking Status",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "All Systems Operational",
                    style = MaterialTheme.typography.labelMedium,
                    color = VigilPalette.Success
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            BlockingStatusRow(
                title = "DNS Filtering",
                subtitle = "Active",
                icon = androidx.compose.material.icons.Icons.Default.Dns,
                status = BlockingStatus.Active
            )
            BlockingStatusRow(
                title = "Root Integration",
                subtitle = "Available",
                icon = androidx.compose.material.icons.Icons.Default.AdminPanelSettings,
                status = BlockingStatus.Active
            )
            BlockingStatusRow(
                title = "Custom Domains",
                subtitle = "12 blocked",
                icon = androidx.compose.material.icons.Icons.Default.ListAlt,
                status = BlockingStatus.Active
            )
        }
    }
}

enum class BlockingStatus { Active, Warning, Inactive }

@Composable
fun BlockingStatusRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    status: BlockingStatus
) {
    val statusColor = when (status) {
        BlockingStatus.Active -> VigilPalette.Success
        BlockingStatus.Warning -> VigilPalette.Warning
        BlockingStatus.Inactive -> VigilPalette.Danger
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "",
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
