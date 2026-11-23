package com.vigil.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.GlassRow
import com.vigil.app.ui.components.GlassSurface
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilPalette
import com.vigil.app.ui.theme.VigilText

@Composable
fun HomeScreen(
    vm: VigilViewModel,
    onOpenProtection: () -> Unit,
    onOpenProgress: () -> Unit
) {
    val protection by vm.protectionEnabled.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val goals by vm.goals.collectAsStateWithLifecycle()
    val domains by vm.domains.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 12.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "VIGIL",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Discipline is a decision.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { ProtectionCard(protection, stats.streak, stats.blocked, onOpenProtection) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = stats.streak.toString(),
                    label = "day streak",
                    icon = VigilIcons.flame,
                    tint = VigilPalette.Warning
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = stats.best.toString(),
                    label = "best run",
                    icon = VigilIcons.trophy,
                    tint = VigilPalette.IcyCyan
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    value = stats.checkIns.toString(),
                    label = "check-ins",
                    icon = VigilIcons.checkCircle,
                    tint = VigilPalette.Success
                )
            }
        }

        item {
            GlassRow(onClick = onOpenProtection) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Protection", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = when {
                            !protection -> "Blocking is off. Nothing is being filtered."
                            else -> "${domains.size} domains in your lists are being enforced."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(Modifier.align(Alignment.TopEnd)) {
                    Icon(VigilIcons.chevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            GlassRow(onClick = onOpenProgress) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Today's goal", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    val goal = goals.firstOrNull()
                    if (goal == null) {
                        Text(
                            "No active goal. Set one in Progress.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(goal.title, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(8.dp))
                        val elapsed =
                            ((System.currentTimeMillis() - goal.startDate) / 86_400_000.0).toInt()
                        val progress = (elapsed.toFloat() / goal.targetDays).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            progress = { progress },
                            color = VigilPalette.IcyCyan
                        )
                        Spacer(Modifier.height(6.dp))
                        val left = (goal.targetDays - elapsed).coerceAtLeast(0)
                        Text(
                            text = "${(progress * 100).toInt()}% · $left days left",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            GlassRow(onClick = onOpenProtection) {
                Column(Modifier.fillMaxWidth()) {
                    Text("Blocked today", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${stats.blocked} requests stopped",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectionCard(
    enabled: Boolean,
    streak: Int,
    blocked: Int,
    onClick: () -> Unit
) {
    val tint = if (enabled) VigilPalette.Success else VigilPalette.Danger
    GlassSurface(modifier = Modifier.fillMaxWidth(), strong = true, onClick = onClick) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (enabled) VigilIcons.shield else VigilIcons.shieldOff,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(54.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (enabled) "PROTECTED" else "EXPOSED",
                style = VigilText.statNumber,
                color = tint
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (enabled) "DNS filtering is enforcing your lists" else "No filtering is running",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                InlineStat("$streak", "streak", VigilIcons.flame)
                InlineStat("$blocked", "blocked", VigilIcons.block)
            }
        }
    }
}

@Composable
private fun InlineStat(value: String, label: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            text = "$value $label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    tint: Color
) {
    GlassRow(modifier = modifier) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = VigilText.statNumber,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


