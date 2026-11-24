package com.vigil.app.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.home.HomeScreen
import com.vigil.app.ui.progress.ProgressScreen
import com.vigil.app.ui.quotes.QuotesScreen
import com.vigil.app.ui.settings.SettingsScreen
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilTheme

private data class NavItem(val name: String, val icon: ImageVector)

private val TABS = listOf(
    NavItem("Home", VigilIcons.flame),
    NavItem("Quotes", VigilIcons.library),
    NavItem("Progress", VigilIcons.calendar),
    NavItem("Settings", VigilIcons.settings)
)

@Composable
fun VigilApp(vm: VigilViewModel = viewModel()) {
    val opts by vm.options.collectAsStateWithLifecycle()
    val dark = when (opts.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    VigilTheme(dark) {
        // A single remembered tab and a plain `when` instead of a navigation
        // back stack: no destination re-inflation, no stacked screens, no
        // fade/slide work on every tap. Tab switches are immediate.
        var tab by rememberSaveable { mutableStateOf("Home") }
        val snackbar = remember { SnackbarHostState() }
        val update by vm.update.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { vm.checkUpdate() }
        LaunchedEffect(vm) { vm.message.collect { snackbar.showSnackbar(it) } }

        if (update != null) {
            AlertDialog(
                onDismissRequest = { vm.skipUpdate() },
                title = { Text("Update available") },
                text = {
                    Column {
                        Text("VIGIL ${update.version} is ready.", style = MaterialTheme.typography.bodyMedium)
                        if (update.notes.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(update.notes, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { vm.downloadAndInstall(update) }) { Text("Download & install") }
                },
                dismissButton = {
                    TextButton(onClick = { vm.skipUpdate() }) { Text("Not now") }
                }
            )
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbar, modifier = Modifier.padding(bottom = 12.dp))
            },
            bottomBar = { VigilNavBar(current = tab, onSelect = { tab = it }) }
        ) { padding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (tab) {
                    "Quotes" -> QuotesScreen(vm)
                    "Progress" -> ProgressScreen(vm)
                    "Settings" -> SettingsScreen(vm)
                    else -> HomeScreen(vm)
                }
            }
        }
    }
}

/** Compact MIUI-style dock: soft selected pill, no expanding indicator. */
@Composable
private fun VigilNavBar(current: String, onSelect: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TABS.forEach { item ->
                val selected = current == item.name
                val pill by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                    else Color.Transparent,
                    animationSpec = tween(140),
                    label = "navPill"
                )
                val tint by animateColorAsState(
                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(140),
                    label = "navTint"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(pill)
                        .clickable { onSelect(item.name) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(
                        item.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint
                    )
                }
            }
        }
    }
}
