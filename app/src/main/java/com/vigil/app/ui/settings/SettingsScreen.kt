package com.vigil.app.ui.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.data.datastore.PrefKeys
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.GlassRow
import com.vigil.app.ui.components.SectionTitle
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilPalette

@Composable
fun SettingsScreen(vm: VigilViewModel) {
    val theme by vm.themeMode.collectAsStateWithLifecycle()
    val glass by vm.glassEnabled.collectAsStateWithLifecycle()
    val reminders by vm.dailyReminders.collectAsStateWithLifecycle()
    val friction by vm.friction.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 8.dp, 0.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle("Appearance") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    Text("Theme", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("system" to "System", "dark" to "Dark", "light" to "Light").forEach { (value, label) ->
                            Choice(label, theme == value, VigilPalette.IcyCyan) { vm.setTheme(value) }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(VigilIcons.sparkle, null, tint = VigilPalette.IcyCyan)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Liquid glass surfaces", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Layered translucent cards with edge highlights",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = glass,
                            onCheckedChange = { vm.setFlag(PrefKeys.LIQUID_GLASS, it) }
                        )
                    }
                }
            }
        }

        item { SectionTitle("Reminders") }

        item {
            GlassRow {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(VigilIcons.notifications, null, tint = VigilPalette.ElectricBlue)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Daily check-in reminder", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "One local notification a day. No servers involved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = reminders,
                        onCheckedChange = { vm.setFlag(PrefKeys.NOTIFY_DAILY, it) }
                    )
                }
            }
        }

        item { SectionTitle("Guardrails") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(VigilIcons.key, null, tint = VigilPalette.Warning)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Pause before disabling", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Shows what you are turning off and asks you to confirm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = friction,
                            onCheckedChange = { vm.setFlag(PrefKeys.FRICTION, it) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    ActionRow("Stop all protection now", VigilPalette.Danger, VigilIcons.emergency, vm::emergencyStop)
                    Text(
                        "There is no anti-uninstall behaviour and no hidden persistence. Removing the app removes everything.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SectionTitle("Privacy") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    PrivacyLine(VigilIcons.device, "Everything stays on this device")
                    PrivacyLine(VigilIcons.noAnalytics, "No analytics, no ads, no accounts")
                    PrivacyLine(VigilIcons.noAccount, "No browsing history is read")
                    PrivacyLine(VigilIcons.privacy, "Only network access is blocklist downloads")
                }
            }
        }

        item { SectionTitle("Your data") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    ActionRow("Copy a text summary", VigilPalette.IcyCyan, VigilIcons.upload) {
                        clipboard.setText(AnnotatedString(vm.exportSummary()))
                        vm.clearMessage()
                    }
                    ActionRow("Delete all local data", VigilPalette.Danger, VigilIcons.deleteForever, vm::wipeData)
                }
            }
        }

        item { SectionTitle("About") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    Text("VIGIL 1.0.0", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Discipline is a decision.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "What it cannot do",
                        style = MaterialTheme.typography.labelLarge,
                        color = VigilPalette.Warning
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "VIGIL filters DNS lookups. It cannot see inside encrypted connections that resolve their own names, cannot judge mixed-content platforms such as Reddit or YouTube, cannot see content inside another app, and cannot stop someone with full device control from removing it. Treat it as a speed bump that supports a decision you already made, not as a wall.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PrivacyLine(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = VigilPalette.Success, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ActionRow(
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.titleSmall, color = tint)
    }
}

@Composable
private fun Choice(
    label: String,
    selected: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) tint.copy(alpha = 0.26f) else MaterialTheme.colorScheme.surfaceContainerHighest
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
