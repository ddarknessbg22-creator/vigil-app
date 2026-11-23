package com.vigil.app.ui.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.blocking.root.RootManager
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.GlassRow
import com.vigil.app.ui.components.SectionTitle
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilPalette

@Composable
fun ProtectionScreen(vm: VigilViewModel) {
    val protection by vm.protectionEnabled.collectAsStateWithLifecycle()
    val dns by vm.dnsEnabled.collectAsStateWithLifecycle()
    val rootEnabled by vm.rootEnabled.collectAsStateWithLifecycle()
    val rootStatus by vm.rootStatus.collectAsStateWithLifecycle()
    val autoUpdate by vm.autoUpdate.collectAsStateWithLifecycle()
    val logBlocked by vm.logBlocked.collectAsStateWithLifecycle()
    val friction by vm.friction.collectAsStateWithLifecycle()
    val domains by vm.domains.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val vpnRunning by vm.vpnRunning.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp, 8.dp, 0.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SectionTitle("Master switch", "Everything below depends on this") }

        item {
            GlassRow {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (protection) VigilIcons.shield else VigilIcons.shieldOff,
                        null,
                        tint = if (protection) VigilPalette.Success else VigilPalette.Danger
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (protection) "Protection on" else "Protection off",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = if (vpnRunning) "VPN tunnel is up" else "No tunnel is running",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = protection, onCheckedChange = vm::setProtection)
                }
            }
        }

        item { SectionTitle("Filtering") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    ToggleRow("DNS filtering", "Routes DNS through VIGIL and answers blocked names itself", dns) {
                        vm.setFlag(com.vigil.app.data.datastore.PrefKeys.DNS_ENABLED, it)
                    }
                    ToggleRow("Auto blocklist updates", "Refresh the adult-domain list in the background", autoUpdate) {
                        vm.setFlag(com.vigil.app.data.datastore.PrefKeys.AUTO_UPDATE, it)
                    }
                    ToggleRow("Log blocked requests", "Store blocked hostnames on this device only", logBlocked) {
                        vm.setFlag(com.vigil.app.data.datastore.PrefKeys.LOG_BLOCKED, it)
                    }
                }
            }
        }

        item {
            GlassRow(onClick = { if (!busy) vm.updateBlocklist() }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(VigilIcons.download, null, tint = VigilPalette.IcyCyan)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Update blocklist now", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = if (busy) "Working…" else "${domains.size} domains stored",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { SectionTitle("Root integration", "Optional, uses the hosts file") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (rootStatus) {
                                RootManager.Status.AVAILABLE -> VigilIcons.checkCircle
                                RootManager.Status.DENIED -> VigilIcons.warning
                                else -> VigilIcons.cancel
                            },
                            null,
                            tint = when (rootStatus) {
                                RootManager.Status.AVAILABLE -> VigilPalette.Success
                                RootManager.Status.DENIED -> VigilPalette.Warning
                                else -> VigilPalette.Danger
                            }
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Root access", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = rootStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = vm::retryRoot) { Icon(VigilIcons.restore, "Recheck root") }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallButton("Write hosts file", onClick = vm::applyRootHosts)
                        SmallButton("Remove hosts entries", onClick = vm::clearRootHosts)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Hosts blocking is system-wide and survives a reboot while the entries stay in place.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ToggleRow("Keep root rules", "Keep hosts entries in sync with your lists", rootEnabled) {
                        vm.setFlag(com.vigil.app.data.datastore.PrefKeys.ROOT_ENABLED, it)
                    }
                }
            }
        }

        item { SectionTitle("Custom domains", "Yours, stored locally") }

        item { DomainInput(onAdd = vm::addDomain) }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    val custom = domains.filter { it.source == "custom" }
                    if (custom.isEmpty()) {
                        Text(
                            "No custom domains yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        custom.forEach { entry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (entry.type == "allowed") VigilIcons.checkCircle else VigilIcons.block,
                                    null,
                                    tint = if (entry.type == "allowed") VigilPalette.Success else VigilPalette.Danger,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = entry.domain,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = entry.type,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(onClick = { vm.removeDomain(entry.domain) }) {
                                    Icon(VigilIcons.delete, "Remove ${entry.domain}")
                                }
                            }
                        }
                    }
                }
            }
        }

        item { SectionTitle("Verify", "Runs the same matcher the filter uses") }

        item { TestInput(onTest = vm::testBlocking) }

        item { SectionTitle("Guardrails") }

        item {
            GlassRow {
                Column(Modifier.fillMaxWidth()) {
                    ToggleRow("Friction before disabling", "Adds a pause and a summary before protection turns off", friction) {
                        vm.setFlag(com.vigil.app.data.datastore.PrefKeys.FRICTION, it)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "VIGIL never blocks uninstalling, never hides itself and never fights the device owner. Stopping protection is always one confirmed tap away.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            GlassRow(onClick = vm::emergencyStop) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(VigilIcons.emergency, null, tint = VigilPalette.Danger)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Emergency stop",
                            style = MaterialTheme.typography.titleSmall,
                            color = VigilPalette.Danger
                        )
                        Text(
                            "Turns every block off right now",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    detail: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, style = MaterialTheme.typography.titleSmall)
            Text(
                detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SmallButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(VigilPalette.IcyCyan.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = VigilPalette.IcyCyan
        )
    }
}

@Composable
private fun DomainInput(onAdd: (String, Boolean) -> Unit) {
    var text by remember { mutableStateOf("") }
    var allowed by remember { mutableStateOf(false) }
    GlassRow {
        Column(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Domain") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallButton("Block") { allowed = false }
                SmallButton("Allow") { allowed = true }
                Spacer(Modifier.weight(1f))
                SmallButton("Add") {
                    if (text.isNotBlank()) {
                        onAdd(text, allowed)
                        text = ""
                    }
                }
            }
        }
    }
}

@Composable
private fun TestInput(onTest: (String) -> Unit) {
    var host by remember { mutableStateOf("") }
    GlassRow {
        Column(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = host,
                onValueChange = { host = it },
                label = { Text("Domain to test") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallButton("Test now") {
                    if (host.isNotBlank()) onTest(host)
                }
                SmallButton("pornhub.com") { host = "pornhub.com"; onTest("pornhub.com") }
                SmallButton("example.com") { host = "example.com"; onTest("example.com") }
            }
        }
    }
}
