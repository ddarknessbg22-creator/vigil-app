package com.vigil.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.home.HomeScreen
import com.vigil.app.ui.progress.ProgressScreen
import com.vigil.app.ui.settings.SettingsScreen
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilTheme
import com.vigil.app.ui.components.themeWave

private data class NavItem(val name: String, val icon: ImageVector)

private val TABS = listOf(
    NavItem("Home", VigilIcons.flame),
    NavItem("Progress", VigilIcons.calendar),
    NavItem("Settings", VigilIcons.settings)
)

private fun mb(bytes: Long): String = when {
    bytes >= 1_048_576 -> String.format(java.util.Locale.US, "%.1f MB", bytes / 1_048_576.0)
    else -> String.format(java.util.Locale.US, "%d KB", bytes / 1024)
}

@Composable
fun VigilApp(vm: VigilViewModel = viewModel()) {
    val opts by vm.options.collectAsStateWithLifecycle()
    val dark = when (opts.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    VigilTheme(dark) {
        var tab by rememberSaveable { mutableStateOf("Home") }
        val snackbar = remember { SnackbarHostState() }
        val update by vm.update.collectAsStateWithLifecycle()
        val download by vm.progress.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) { vm.checkUpdate() }
        LaunchedEffect(vm) { vm.message.collect { snackbar.showSnackbar(it) } }

        val updateInfo = update
        if (updateInfo != null) {
            SoftwareUpdateSheet(
                version = updateInfo.version,
                notes = updateInfo.notes,
                published = updateInfo.published,
                size = updateInfo.size,
                download = download,
                onDownload = { vm.downloadAndInstall(updateInfo) },
                onCancel = { vm.cancelDownload() },
                onDismiss = { vm.skipUpdate() }
            )
        }

        Scaffold(
            modifier = Modifier.themeWave(dark.toString()),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar, modifier = Modifier.padding(bottom = 12.dp)) },
            bottomBar = { VigilNavBar(current = tab, onSelect = { tab = it }) }
        ) { padding ->
            // iOS tab bars switch instantly — no crossfade of two full screens.
            // Composing two screens at once (AnimatedContent) was the tab lag.
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    "Progress" -> ProgressScreen(vm)
                    "Settings" -> SettingsScreen(vm)
                    else -> HomeScreen(vm)
                }
            }
        }
    }
}

/**
 * "Software Update" sheet — the dark, centered iOS-style dialog:
 * title, "Downloading VIGIL x…", hairline progress, "What's new" artwork,
 * "Top Features" bullets, and a full-width separated Cancel button.
 */
@Composable
private fun SoftwareUpdateSheet(
    version: String,
    notes: String,
    published: String,
    size: String,
    download: VigilViewModel.DownloadProgress?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = { if (download == null) onDismiss() }) {
        val enter = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            enter.animateTo(1f, spring(dampingRatio = 0.82f, stiffness = 340f))
        }
        val eased = 1f - (1f - enter.value) * (1f - enter.value)

        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF1C1C1E),
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .wrapContentHeight()
                .shadow(28.dp, RoundedCornerShape(26.dp))
                .graphicsLayer {
                    alpha = eased
                    scaleX = 0.92f + eased * 0.08f
                    scaleY = 0.92f + eased * 0.08f
                    translationY = (1f - eased) * 36f
                }
        ) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                // Header
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp)) {
                    Text(
                        "Software Update",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        if (download == null) "VIGIL $version is ready" else "Downloading VIGIL $version…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFB0B0B8),
                        modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(14.dp))

                    // Progress: thin hairline bar, no rounded pill.
                    val pct = if (download?.total ?: 0 > 0)
                        (download!!.done * 100 / download.total).toInt().coerceIn(0, 100) else 0
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF3A3A3C))
                    ) {
                        if (download == null) {
                            Box(Modifier.fillMaxWidth(0.12f).height(4.dp).background(Color(0xFF0A84FF)))
                        } else {
                            val animated by animateFloatAsState(
                                if (download.total > 0) download.done.toFloat() / download.total else 0f,
                                tween(180), label = "downloadBar"
                            )
                            Box(
                                Modifier.fillMaxWidth(animated.coerceIn(0.01f, 1f)).height(4.dp)
                                    .background(Color(0xFF0A84FF))
                            )
                        }
                    }
                    if (download != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "$pct%  ·  ${mb(download.done)} of ${mb(download.total)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF8E8E96),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Divider
                Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF3A3A3C)))

                // What's new — artwork panel + bullets
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)) {
                    Text(
                        "What's new",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))

                    // Original VIGIL artwork block (no external images).
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                linearGradientCompat()
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "V I G I L",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                version,
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                            Text(
                                "Discipline is a decision.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Top Features",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    notes.lineSequence()
                        .map { it.trim().removePrefix("• ").removePrefix("- ") }
                        .filter { it.isNotBlank() && !it.startsWith("#") }
                        .take(6)
                        .forEach { feature ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                                Text("•", color = Color(0xFF0A84FF), modifier = Modifier.padding(end = 8.dp))
                                Text(feature, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD8D8DE))
                            }
                        }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Released $published  ·  $size",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7E7E86),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Divider
                Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF3A3A3C)))

                // Full-width actions, separated like iOS action sheets.
                if (download == null) {
                    Text(
                        "Download & install",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF0A84FF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDownload() }
                            .padding(vertical = 15.dp),
                        textAlign = TextAlign.Center
                    )
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(Color(0xFF3A3A3C)))
                }
                Text(
                    if (download == null) "Not now" else "Cancel download",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (download == null) onDismiss() else onCancel() }
                        .padding(vertical = 15.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun linearGradientCompat(): Brush = Brush.linearGradient(
    listOf(Color(0xFF0A2A5E), Color(0xFF123A7A), Color(0xFF0A84FF))
)

/** Compact iOS dock. */
@Composable
private fun VigilNavBar(current: String, onSelect: (String) -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.94f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            TABS.forEach { item ->
                val selected = current == item.name
                val pill by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else Color.Transparent,
                    tween(160), label = "navPill"
                )
                val tint by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    tween(160), label = "navTint"
                )
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(pill)
                        .clickable { onSelect(item.name) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.height(3.dp))
                    Text(item.name, style = MaterialTheme.typography.labelSmall, color = tint)
                }
            }
        }
    }
}
