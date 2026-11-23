package com.vigil.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.vigil.app.ui.VigilViewModel
import com.vigil.app.ui.home.HomeScreen
import com.vigil.app.ui.progress.ProgressScreen
import com.vigil.app.ui.quotes.QuotesScreen
import com.vigil.app.ui.settings.SettingsScreen
import com.vigil.app.ui.theme.VigilIcons
import com.vigil.app.ui.theme.VigilTheme

@Composable
fun VigilApp(vm: VigilViewModel = viewModel()) {
    val opts by vm.options.collectAsStateWithLifecycle()
    val dark = when (opts.theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

    VigilTheme(dark) {
        val nav = rememberNavController()
        val entry by nav.currentBackStackEntryAsState()
        val snackbar = remember { SnackbarHostState() }

        val tabs = remember {
            listOf(
                "Home" to VigilIcons.flame,
                "Quotes" to VigilIcons.library,
                "Progress" to VigilIcons.calendar,
                "Settings" to VigilIcons.settings
            )
        }

        LaunchedEffect(vm) { vm.message.collect { snackbar.showSnackbar(it) } }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            // Navigation uses the same elevated card colour as the surfaces
            // above it, with a thin divider, so it no longer reads as a
            // black strip pasted under the page.
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    tabs.forEach { (name, icon) ->
                        val selected = entry?.destination?.route == name
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(name) {
                                    popUpTo("Home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = nav,
                startDestination = "Home",
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 12 } },
                exitTransition = { fadeOut(tween(140)) },
                popEnterTransition = { fadeIn(tween(220)) },
                popExitTransition = { fadeOut(tween(140)) + slideOutHorizontally(tween(220)) { it / 12 } }
            ) {
                composable("Home") { HomeScreen(vm) }
                composable("Quotes") { QuotesScreen(vm) }
                composable("Progress") { ProgressScreen(vm) }
                composable("Settings") { SettingsScreen(vm) }
            }
        }
    }
}
