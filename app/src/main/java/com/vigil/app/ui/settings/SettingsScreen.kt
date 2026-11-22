package com.vigil.app.ui.settings

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
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp, 20.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { TopBar(onBack = onNavigateBack, title = "Settings") }
        item { ProtectionConfigSection() }
        item { NotificationsSection() }
        item { ThemeSection() }
        item { PrivacySection() }
        item { DataSection() }
        item { RecoverySection() }
        item { AboutSection() }
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
fun ProtectionConfigSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Protection Configuration", "Core protection settings", androidx.compose.material.icons.Icons.Default.Security, VigilPalette.IcyCyan)
            SettingsToggle("Enable Protection", "Master switch for all blocking features", true, { })
            SettingsToggle("Block Adult Content", "Use built-in adult content blocklists", true, { })
            SettingsToggle("Custom Blocklist", "Enable your personal blocked domains", true, { })
            SettingsToggle("Allowlist", "Domains that are never blocked", false, { })
            SettingsToggle("App Restrictions Enabled", "Restrict access to selected applications", true, { })
            SettingsToggle("Schedule Protection", "Automatically enable/disable on schedule", false, { })
            SettingsToggle("Cooldown Period", "Delay before protection can be disabled", true, { })
            SettingsToggle("Friction Mode", "Add extra steps to disable protection", true, { })
            SettingsToggle("Emergency Disable", "Always allow immediate protection disable", true, { })
        }
    }
}

@Composable
fun NotificationsSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Notifications", "Configure reminders and alerts", androidx.compose.material.icons.Icons.Default.Notifications, VigilPalette.ElectricBlue)
            SettingsToggle("Daily Reminders", "Reminder to check in daily", true, { })
            SettingsToggle("Check-in Reminder", "Notification at your preferred time", true, { })
            SettingsToggle("Protection Status Alerts", "Notify when protection is weakened", true, { })
            SettingsToggle("Weekly Summary", "Weekly progress report", true, { })
            SettingsToggle("Milestone Notifications", "Celebrate achievements", true, { })
        }
    }
}

@Composable
fun ThemeSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Theme", "Appearance settings", androidx.compose.material.icons.Icons.Default.Palette, VigilPalette.Warning)
            SettingItem("Theme Mode", "Dark (default)", androidx.compose.material.icons.Icons.Default.DarkMode, VigilPalette.IcyCyan, { })
            SettingItem("Liquid Glass Effects", "Enable advanced glass rendering", androidx.compose.material.icons.Icons.Default.AutoAwesome, VigilPalette.ElectricBlue, { })
            SettingItem("Reduced Motion", "Minimize animations", androidx.compose.material.icons.Icons.Default.Accessibility, VigilPalette.Success, { })
        }
    }
}

@Composable
fun PrivacySection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Privacy", "Data and privacy controls", androidx.compose.material.icons.Icons.Default.PrivacyTip, VigilPalette.Success)
            SettingItem("Local Data Only", "All data stored on device, never uploaded", androidx.compose.material.icons.Icons.Default.DeviceUnknown, VigilPalette.IcyCyan, { })
            SettingItem("No Analytics", "Zero tracking, zero telemetry", androidx.compose.material.icons.Icons.Default.Analytics, VigilPalette.ElectricBlue, { })
            SettingItem("No Accounts Required", "Use fully offline, no sign-up", androidx.compose.material.icons.Icons.Default.PersonOff, VigilPalette.Success, { })
            SettingItem("Privacy Policy", "Read our privacy commitments", androidx.compose.material.icons.Icons.Default.Description, VigilPalette.Warning, { })
        }
    }
}

@Composable
fun DataSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Data Management", "Export, backup, or delete your data", androidx.compose.material.icons.Icons.Default.Storage, VigilPalette.ElectricBlue)
            SettingsButton("Export All Data", "Download complete backup (JSON)", androidx.compose.material.icons.Icons.Default.Download, VigilPalette.Success, { })
            SettingsButton("Import Data", "Restore from a previous export", androidx.compose.material.icons.Icons.Default.Upload, VigilPalette.IcyCyan, { })
            SettingsButton("Delete All Data", "Permanently erase all personal data", androidx.compose.material.icons.Icons.Default.DeleteForever, VigilPalette.Danger, { })
        }
    }
}

@Composable
fun RecoverySection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("Recovery Options", "Emergency and safety features", androidx.compose.material.icons.Icons.Default.MedicalServices, VigilPalette.Danger)
            SettingsButton("Emergency Disable Protection", "Immediately stop all blocking", androidx.compose.material.icons.Icons.Default.Emergency, VigilPalette.Danger, { })
            SettingsButton("Reset Protection Settings", "Restore default protection config", androidx.compose.material.icons.Icons.Default.Restore, VigilPalette.Warning, { })
            SettingsButton("Generate Recovery Code", "One-time code for emergency access", androidx.compose.material.icons.Icons.Default.VpnKey, VigilPalette.IcyCyan, { })
        }
    }
}

@Composable
fun AboutSection() {
    GlassRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SectionTitle("About", "App information and credits", androidx.compose.material.icons.Icons.Default.Info, VigilPalette.ElectricBlue)
            SettingItem("Version", "1.0.0 (build 1)", androidx.compose.material.icons.Icons.Default.Tag, VigilPalette.IcyCyan, { })
            SettingItem("Open Source Licenses", "Third-party library acknowledgments", androidx.compose.material.icons.Icons.Default.LibraryBooks, VigilPalette.Success, { })
            SettingItem("Known Limitations", "What VIGIL cannot block", androidx.compose.material.icons.Icons.Default.Warning, VigilPalette.Warning, { })
            SettingItem("Acknowledgments", "Blocklist maintainers and contributors", androidx.compose.material.icons.Icons.Default.Favorite, VigilPalette.Danger, { })
        }
    }
}

@Composable
fun SettingsToggle(
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
fun SettingItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
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
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color = iconColor.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = "", tint = iconColor, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = "", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsButton(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
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
            androidx.compose.foundation.layout.Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(color = iconColor.copy(alpha = 0.15f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = "", tint = iconColor, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text(text = title, style = MaterialTheme.typography.bodyMedium, color = iconColor)
                    Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(imageVector = androidx.compose.material.icons.Icons.Default.ChevronRight, contentDescription = "", tint = iconColor)
        }
    }
}
