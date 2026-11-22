package com.vigil.app.ui.protection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
fun ProtectionScreen(
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TopBar(onBack = onNavigateBack, title = "Protection")
        }

        item { DnsSection() }
        item { BlocklistSection() }
        item { CustomDomainsSection() }
        item { RootSection() }
        item { AppRestrictionsSection() }
        item { BlockingTestsSection() }
        item { RecoverySection() }
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
fun DnsSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("DNS Filtering", "Core protection layer", androidx.compose.material.icons.Icons.Default.Dns, VigilPalette.IcyCyan)
            ProtectionToggle("Enable DNS Filtering", "Blocks known adult domains at the network level", true, { })
            ProtectionToggle("Auto-update Blocklists", "Download latest blocklists automatically", true, { })
            ProtectionToggle("Log Blocked Requests", "Record blocked domains for review", false, { })
        }
    }
}

@Composable
fun BlocklistSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Blocklist Management", "Configure which lists to use", androidx.compose.material.icons.Icons.Default.ListAlt, VigilPalette.ElectricBlue)
            BlocklistItem("OISD Full", "Comprehensive adult content blocklist (~2M domains)", true, "2 hours ago", { })
            BlocklistItem("OISD Basic", "Essential adult domains only (~500K domains)", false, "2 hours ago", { })
            BlocklistItem("Custom Blocklist", "Your personal domain list", true, "Just now", { })
        }
    }
}

@Composable
fun CustomDomainsSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Custom Domains", "Add your own blocked/allowed domains", androidx.compose.material.icons.Icons.Default.Edit, VigilPalette.Success)
            CustomDomainRow("example-adult.com", DomainType.Blocked, { })
            CustomDomainRow("tracking-site.net", DomainType.Blocked, { })
            CustomDomainRow("allowed-site.org", DomainType.Allowed, { })
            AddDomainButton { }
        }
    }
}

@Composable
fun RootSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Root Integration", "Enhanced blocking capabilities", androidx.compose.material.icons.Icons.Default.AdminPanelSettings, VigilPalette.Warning)
            RootStatusRow("Root Access", RootStatus.Available, "Magisk/APatch detected")
            ProtectionToggle("Enable Root-based Blocking", "Use hosts file and iptables for system-wide blocking", true, { })
            ProtectionToggle("Boot Persistence", "Re-apply rules on device restart", true, { })
            ProtectionToggle("DNS-over-HTTPS Bypass", "Block DoH endpoints to prevent bypass", false, { })
        }
    }
}

@Composable
fun AppRestrictionsSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("App Restrictions", "Limit access to distracting apps", androidx.compose.material.icons.Icons.Default.Apps, VigilPalette.ElectricBlue)
            AppRestrictionRow("Instagram", "com.instagram.android", true, { })
            AppRestrictionRow("TikTok", "com.zhiliaoapp.musically", true, { })
            AppRestrictionRow("Reddit", "com.reddit.frontpage", false, { })
            AddAppButton { }
        }
    }
}

@Composable
fun BlockingTestsSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Blocking Tests", "Verify protection is working", androidx.compose.material.icons.Icons.Default.Science, VigilPalette.Success)
            TestButton("Test Adult Domain Block", "Attempts to resolve known adult domain", { })
            TestButton("Test Custom Domain Block", "Verifies your custom blocked domains", { })
            TestButton("Test Allowlist", "Confirms allowed domains resolve correctly", { })
            TestButton("Full Blocking Audit", "Comprehensive protection verification", { })
        }
    }
}

@Composable
fun RecoverySection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Emergency Recovery", "Always accessible safety mechanisms", androidx.compose.material.icons.Icons.Default.Emergency, VigilPalette.Danger)
            RecoveryButton("Emergency Disable Protection", "Immediately stop all blocking (requires confirmation)", VigilPalette.Danger, { })
            RecoveryButton("Export Configuration", "Backup your settings and blocklists", VigilPalette.ElectricBlue, { })
            RecoveryButton("Reset to Defaults", "Clear all custom settings, keep blocklists", VigilPalette.Warning, { })
        }
    }
}

@Composable
fun ProtectionToggle(
    label: String,
    description: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = VigilPalette.IcyCyan,
                checkedTrackColor = VigilPalette.IcyCyan.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun BlocklistItem(
    name: String,
    description: String,
    isEnabled: Boolean,
    lastUpdated: String,
    onToggle: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                if (lastUpdated != "Never") {
                    Text(text = "Updated $lastUpdated", style = MaterialTheme.typography.labelSmall, color = VigilPalette.Success)
                } else {
                    Text(text = "Not installed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = VigilPalette.IcyCyan,
                checkedTrackColor = VigilPalette.IcyCyan.copy(alpha = 0.3f)
            )
        )
    }
}

enum class DomainType { Blocked, Allowed }

@Composable
fun CustomDomainRow(
    domain: String,
    type: DomainType,
    onDelete: () -> Unit
) {
    val (color, iconRes) = when (type) {
        DomainType.Blocked -> VigilPalette.Danger to androidx.compose.material.icons.Icons.Default.Block
        DomainType.Allowed -> VigilPalette.Success to androidx.compose.material.icons.Icons.Default.CheckCircle
    }

    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color = color.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconRes, contentDescription = "", tint = color, modifier = Modifier.size(16.dp))
            }
            Text(
                text = domain,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
        IconButton(onClick = onDelete) {
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddDomainButton(onClick: () -> Unit) {
    GlassRow(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add Custom Domain", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

enum class RootStatus { Available, NotAvailable, Denied }

@Composable
fun RootStatusRow(
    title: String,
    status: RootStatus,
    description: String
) {
    val (color, iconRes) = when (status) {
        RootStatus.Available -> VigilPalette.Success to androidx.compose.material.icons.Icons.Default.CheckCircle
        RootStatus.NotAvailable -> MaterialTheme.colorScheme.onSurfaceVariant to androidx.compose.material.icons.Icons.Default.Cancel
        RootStatus.Denied -> VigilPalette.Warning to androidx.compose.material.icons.Icons.Default.Warning
    }

    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(color = color.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = iconRes, contentDescription = "", tint = color, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text(
            text = when (status) {
                RootStatus.Available -> "Available"
                RootStatus.NotAvailable -> "Not Available"
                RootStatus.Denied -> "Denied"
            },
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
fun AppRestrictionRow(
    appName: String,
    packageName: String,
    isRestricted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceContainerHighest, shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.Android, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = appName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
            }
        }
        Switch(
            checked = isRestricted,
            onCheckedChange = onToggle,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = VigilPalette.IcyCyan,
                checkedTrackColor = VigilPalette.IcyCyan.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
fun AddAppButton(onClick: () -> Unit) {
    GlassRow(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Add, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Add Restricted App", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TestButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    GlassRow(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.PlayArrow, contentDescription = "Run test", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun RecoveryButton(
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    GlassRow(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, color = color)
                Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = "", tint = color)
        }
    }
}
