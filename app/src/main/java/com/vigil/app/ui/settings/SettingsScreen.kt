package com.vigil.app.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vigil.app.BuildConfig
import com.vigil.app.data.datastore.Options
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.*
import com.vigil.app.ui.components.IosSwitch
import com.vigil.app.ui.theme.VigilIcons
import java.time.LocalTime

/**
 * Settings, rebuilt as an iOS-style grouped list.
 *
 * Booleans use real switches with descriptive labels — no more "tap a row and
 * hope it toggled back". Only settings that matter to this app are present.
 */
@Composable
fun SettingsScreen(vm: VigilViewModel) {
    val o by vm.options.collectAsStateWithLifecycle()
    val all by vm.quotes.collectAsStateWithLifecycle()
    val runs by vm.runs.collectAsStateWithLifecycle()
    val current = runs.firstOrNull { it.end == null }
    val context = LocalContext.current

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

    Page("Settings", "VIGIL / Make it yours") {
        SectionHeader("CHECK-IN")
        SettingsGroup {
            SettingsValueRow(
                title = "Clean since",
                value = current?.let { dateLabel(it.start) } ?: "Not started",
                onClick = { if (current == null) pickStart(context, vm::start) else sheet = "streak" }
            )
            SettingsValueRow(
                title = "Daily check-in time",
                value = LocalTime.of(o.minute / 60, o.minute % 60).toString(),
                onClick = { showTimePicker(context, o) { vm.notifications(o.reminder, it, o.quoteNotice) } }
            )
            SwitchRow(
                title = "Gentle reminder",
                detail = "One local notification near your chosen time",
                checked = o.reminder,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= 33 &&
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        perm.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.notifications(enabled, o.minute, o.quoteNotice)
                    }
                }
            )
            if (denied) {
                Text(
                    "Notifications are off. Enable them in Android settings if you want reminders.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }
            SwitchRow(
                title = "Quote in reminder",
                detail = "A short clean-days line with the reminder",
                checked = o.quoteNotice,
                enabled = o.reminder,
                onCheckedChange = { vm.notifications(o.reminder, o.minute, it) }
            )
        }

        SectionHeader("APPEARANCE")
        SettingsGroup {
            Text("Theme", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = (8.dp)))
            SegmentedControl(
                items = listOf("system" to "System", "light" to "Light", "dark" to "Dark"),
                selected = o.theme,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = (6.dp))
            ) { vm.theme(it) }
        }

        SectionHeader("UPDATES")
        SettingsGroup {
            SettingsValueRow(
                title = "Check for updates",
                value = "v${BuildConfig.VERSION_NAME}",
                onClick = { vm.checkUpdate(true) }
            )
            Text(
                "Updates are checked on launch. You can skip a prompt and it will ask again next time.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = (8.dp))
            )
        }

        SectionHeader("PRIVACY")
        SettingsGroup {
            SettingsValueRow("Export journal", "Private JSON backup") { backup.launch("vigil-journal.json") }
            SettingsValueRow("Restore backup", "Replace this device's journal") { sheet = "restore" }
            Text(
                "No account, no ads, no analytics. Your journal never leaves this device unless you export it.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = (8.dp))
            )
        }

        SectionHeader("ABOUT")
        SettingsGroup {
            Text("VIGIL ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = (8.dp)))
            Text(
                "A private journal for staying clean from pornography and gooning. The clock measures time, not your worth.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = (8.dp)))
        }
    }

    if (sheet.isNotEmpty()) Sheet(sheetTitle(sheet), { sheet = "" }) {
        when (sheet) {
            "streak" -> {
                Text(
                    "Your clean days, personal best and journal stay here. The daily decision opens once a day at your chosen time — one answer per day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChangeStartDateRow(context, vm, onDone = { sheet = "" })
            }

            "category" -> {
                (listOf("Any") + all.map { it.category }.distinct()).forEach { name ->
                    SelectionRow(name, o.category == name) { vm.category(name); sheet = "" }
                }
            }

            "restore" -> {
                Text(
                    "A valid backup replaces your streaks, goals, reflections and saved quotes. Export first if you want a copy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        sheet = ""
                        restore.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
                    },
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Choose backup file") }
            }
        }
    }
}

private fun sheetTitle(key: String) = when (key) {
    "streak" -> "Your streak"
    "category" -> "Daily quote theme"
    "restore" -> "Restore backup"
    else -> "Settings"
}

private fun showTimePicker(c: android.content.Context, o: Options, onTime: (Int) -> Unit) {
    TimePickerDialog(c, { _, h, m -> onTime(h * 60 + m) }, o.minute / 60, o.minute % 60, true).show()
}

/**
 * Changing the start date is deliberately harder than a single tap:
 * you must hold the button for 2 seconds before the date picker opens.
 */
@Composable
private fun ChangeStartDateRow(
    context: android.content.Context,
    vm: VigilViewModel,
    onDone: () -> Unit
) {
    var confirming by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }

    if (!confirming) {
        ActionRow("Change starting date") { confirming = true }
        return
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "This restarts your clean run from the date you choose. Past days stay in your journal, but the counter begins again. Are you sure?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.22f))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                val start = System.currentTimeMillis()
                                val released = tryAwaitRelease()
                                if (released && System.currentTimeMillis() - start >= 2000) {
                                    ready = true
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (ready) "Ready — choose a new date" else "Hold for 2 seconds to continue",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = { confirming = false; ready = false }) { Text("Cancel") }
                Button(
                    enabled = ready,
                    onClick = {
                        onDone()
                        confirming = false
                        ready = false
                        pickStart(context, vm::start)
                    }
                ) { Text("Continue") }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp, top = 6.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(vertical = 6.dp), content = content)
    }
}

@Composable
private fun SettingsValueRow(title: String, value: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(VigilIcons.chevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SwitchRow(
    title: String,
    detail: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(detail, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IosSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SegmentedControl(
    items: List<Pair<String, String>>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(3.dp)) {
            items.forEach { (value, label) ->
                val active = selected == value
                val background by animateColorAsState(
                    if (active) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent,
                    label = "segment"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(background)
                        .clickable(interactionSource = remember { MutableInteractionSource() },
                            indication = null) { onSelect(value) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge,
                        color = if (active) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun SelectionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() },
                indication = null, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        AnimatedVisibility(visible = selected, enter = fadeIn(tween(160))) {
            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp))
        }
    }
}
