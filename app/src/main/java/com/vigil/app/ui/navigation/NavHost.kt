package com.vigil.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
            UpdateSheet(
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
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(snackbar, modifier = Modifier.padding(bottom = 12.dp)) },
            bottomBar = { VigilNavBar(current = tab, onSelect = { tab = it }) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
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

/** iOS-style update sheet: spring entry, animated ring, breathing accent. */
@Composable
private fun UpdateSheet(
    version: String,
    notes: String,
    published: String,
    size: String,
    download: VigilViewModel.DownloadProgress?,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (download == null) onDismiss() }
    ) {
        val entry = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            entry.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 320f))
        }

        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 0.dp,
            shadowElevation = 32.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .graphicsLayer {
                    val eased = 1f - (1f - entry.value) * (1f - entry.value)
                    alpha = eased
                    scaleX = 0.9f + eased * 0.1f
                    scaleY = 0.9f + eased * 0.1f
                    translationY = (1f - eased) * 48f
                }
        ) {
            Column(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(width = 38.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outline)
                )

                Spacer(Modifier.height(20.dp))

                // Animated icon: ring scale + subtle rotation while downloading.
                val infinite = rememberInfiniteTransition(label = "updatePulse")
                val pulse by infinite.animateFloat(
                    0.96f, 1.04f,
                    infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "pulse"
                )
                val rotation by infinite.animateFloat(
                    -4f, 4f,
                    infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                    label = "tilt"
                )
                val iconScale = if (download == null) pulse else 1f

                Box(
                    Modifier
                        .size(64.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                            rotationZ = rotation
                        }
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (download == null) VigilIcons.download else VigilIcons.trendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    if (download == null) "Update available" else "Downloading VIGIL $version",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Released $published  ·  $size",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(14.dp))

                if (download == null) {
                    Text(
                        notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val pct = if (download.total > 0)
                        (download.done * 100 / download.total).toInt().coerceIn(0, 100) else 0
                    val animatedPct by animateIntAsState(pct, tween(240), label = "pct")
                    LinearProgressIndicator(
                        progress = {
                            if (download.total > 0) download.done.toFloat() / download.total else 0f
                        },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "$animatedPct%  ·  ${mb(download.done)} of ${mb(download.total)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = if (download == null) onDownload else onCancel,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (download == null) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Text(
                        if (download == null) "Download & install" else "Cancel download",
                        color = if (download == null) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = download == null,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Not now") }
            }
        }
    }
}

/** Compact iOS-style dock with a sliding selected pill. */
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
                val scale by animateFloatAsState(if (selected) 1.06f else 1f, tween(160), label = "navScale")
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(pill)
                        .clickable { onSelect(item.name) }
                        .padding(vertical = 8.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale },
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
