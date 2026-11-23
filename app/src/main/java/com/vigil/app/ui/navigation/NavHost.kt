package com.vigil.app.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.components.AmbientBackdrop
import com.vigil.app.ui.home.HomeScreen
import com.vigil.app.ui.protection.ProtectionScreen
import com.vigil.app.ui.progress.ProgressScreen
import com.vigil.app.ui.settings.SettingsScreen
import com.vigil.app.ui.theme.LocalGlassEnabled
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilTheme

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", VigilIcons.shield),
    Protection("Protection", VigilIcons.dns),
    Progress("Progress", VigilIcons.trendingUp),
    Settings("Settings", VigilIcons.settings)
}

@Composable
fun VigilApp(vm: VigilViewModel = viewModel()) {
    val mode by vm.themeMode.collectAsStateWithLifecycle()
    val glass by vm.glassEnabled.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val dark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> systemDark
    }

    VigilTheme(darkTheme = dark) {
        CompositionLocalProvider(LocalGlassEnabled provides glass) {
            VigilScaffold(vm)
        }
    }
}

@Composable
private fun VigilScaffold(vm: VigilViewModel) {
    var current by remember { mutableStateOf(Tab.Home) }
    val snackbar = remember { SnackbarHostState() }
    val message by vm.message.collectAsStateWithLifecycle()

    LaunchedEffect(message) {
        val text = message ?: return@LaunchedEffect
        snackbar.showSnackbar(text)
        vm.clearMessage()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 0.dp
            ) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = current == tab,
                        onClick = { current = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { inner ->
        AmbientBackdrop(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().padding(inner)) {
                when (current) {
                    Tab.Home -> HomeScreen(
                        vm = vm,
                        onOpenProtection = { current = Tab.Protection },
                        onOpenProgress = { current = Tab.Progress }
                    )

                    Tab.Protection -> ProtectionScreen(vm)
                    Tab.Progress -> ProgressScreen(vm)
                    Tab.Settings -> SettingsScreen(vm)
                }
            }
        }
    }
}
