package com.vigil.app.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import com.vigil.app.ui.theme.VigilIcons
import java.time.LocalTime

@Composable
fun SettingsScreen(vm: VigilViewModel) {
    val o by vm.options.collectAsStateWithLifecycle()
    val all by vm.quotes.collectAsStateWithLifecycle()
    val runs by vm.runs.collectAsStateWithLifecycle()
    val current = runs.firstOrNull { it.end == null }
    val c = LocalContext.current

    var sheet by rememberSaveable { mutableStateOf("") }
    var denied by remember { mutableStateOf(false) }

    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) vm.notifications(true, o.minute, o.quoteNotice)
    }
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        it?.let(vm::export)
    }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let(vm::restore)
    }

    Page("Settings", "V I G I L    /    Make it yours") {
        SettingsCard("Your streak", VigilIcons.flame) {
            SettingsRow(
                "Clean since",
                current?.let { dateLabel(it.start) } ?: "Choose a starting date"
            ) {
                if (current == null) pickStart(c, vm::start) else sheet = "My streak"
            }
            SettingsRow("Check-in time", LocalTime.of(o.minute / 60, o.minute % 60).toString()) {
                showTimePicker(c, o.minute) { vm.notifications(o.reminder, it, o.quoteNotice) }
            }
        }

        SettingsCard("Daily rhythm", VigilIcons.notifications) {
            SettingsRow(
                "Gentle reminder",
                if (o.reminder) LocalTime.of(o.minute / 60, o.minute % 60).toString() else "Off"
            ) {
                if (!o.reminder && Build.VERSION.SDK_INT >= 33 &&
                    c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    perm.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    vm.notifications(!o.reminder, o.minute, o.quoteNotice)
                }
            }
            if (denied) {
                Text(
                    "Notifications weren’t enabled. You can allow them in Android settings.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            SettingsRow("Notification text", if (o.quoteNotice) "Daily quote" else "Discreet reminder") {
                vm.notifications(o.reminder, o.minute, !o.quoteNotice)
            }
        }

        SettingsCard("Appearance", VigilIcons.palette) {
            Kicker("Theme")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                    ChoiceChip(label, o.theme == value) { vm.theme(value) }
                }
            }
        }

        SettingsCard("Quotes", VigilIcons.library) {
            Kicker("Daily theme")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip("Any", o.category == "Any") { vm.category("Any") }
                all.map { it.category }.distinct().take(2).forEach { name ->
                    ChoiceChip(name, o.category == name) { vm.category(name) }
                }
            }
            TextButton(onClick = { sheet = "Quote preference" }) { Text("See all themes") }
        }

        SettingsCard("Privacy & backup", VigilIcons.lock) {
            SettingsRow("Export journal", "A private JSON backup") { backup.launch("vigil-journal.json") }
            SettingsRow("Restore backup", "Replace this device’s journal") { sheet = "Replace journal?" }
            Text(
                "Everything stays on this device. No account, advertising or analytics.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsCard("Updates", VigilIcons.download) {
            SettingsRow("Check for updates", "See if a newer version is available") {
                vm.checkUpdate(true)
            }
            Text(
                "Updates come from the official GitHub release. You can skip any prompt and it will ask again next launch.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        SettingsCard("About", VigilIcons.info) {
            Text("VIGIL 2.0", style = MaterialTheme.typography.titleSmall)
            Text(
                "A private journal for staying clean from pornography and gooning. Every day you choose to pass is a day you keep. The clock measures time, not your worth.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("${all.size} offline readings · VIGIL original",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (sheet.isNotEmpty()) Sheet(sheet, { sheet = "" }) {
        when (sheet) {
            "My streak" -> {
                Text(
                    "Your clean days, personal best and journal stay here. The daily decision lives on Home and opens once a day at your chosen time. One answer per day, recorded honestly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { sheet = "" }, shape = RoundedCornerShape(24.dp)) { Text("Back") }
            }

            "Quote preference" -> {
                (listOf("Any") + all.map { it.category }.distinct()).forEach { name ->
                    ActionRow(name, if (name == o.category) "Selected" else "") {
                        vm.category(name); sheet = ""
                    }
                }
            }

            "Replace journal?" -> {
                Text(
                    "A valid backup replaces this device’s streaks, goals, reflections, saved quotes and reading history. Export first if you want a copy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        sheet = ""
                        restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    },
                    shape = RoundedCornerShape(24.dp)
                ) { Text("Choose backup to restore") }
            }
        }
    }
}

private fun showTimePicker(
    c: android.content.Context,
    minute: Int,
    onTime: (Int) -> Unit
) {
    TimePickerDialog(c, { _, h, m -> onTime(h * 60 + m) }, minute / 60, minute % 60, true).show()
}

@Composable
private fun SettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(title: String, detail: String, onClick: () -> Unit) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(vertical = 10.dp)
    Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(VigilIcons.chevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChoiceChip(label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) }
    )
}
