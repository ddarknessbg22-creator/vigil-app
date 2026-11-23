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
import com.vigil.app.ui.theme.*

@Composable fun VigilApp(vm:VigilViewModel=viewModel()){
    val opts by vm.options.collectAsStateWithLifecycle();val dark=when(opts.theme){"light"->false;"dark"->true;else->isSystemInDarkTheme()}
    VigilTheme(dark){ val nav=rememberNavController();val entry by nav.currentBackStackEntryAsState();val snackbar=remember { SnackbarHostState() };val tabs=remember { listOf("Home" to VigilIcons.flame,"Quotes" to VigilIcons.library,"Progress" to VigilIcons.calendar,"Settings" to VigilIcons.settings) }
        LaunchedEffect(vm){vm.message.collect { snackbar.showSnackbar(it) }}
        Scaffold(snackbarHost={SnackbarHost(snackbar)},bottomBar={NavigationBar(containerColor=MaterialTheme.colorScheme.background.copy(alpha=0.96f),tonalElevation=0.dp){tabs.forEach { (name,icon)->NavigationBarItem(selected=entry?.destination?.route==name,onClick={nav.navigate(name){popUpTo("Home"){saveState=true};launchSingleTop=true;restoreState=true}},icon={Icon(icon,null)},label={Text(name)}) }}}){padding->NavHost(navController=nav,startDestination="Home",modifier=Modifier.padding(padding),enterTransition={fadeIn(tween(220))+scaleIn(tween(220),initialScale=0.97f)},exitTransition={fadeOut(tween(140))}){composable("Home"){HomeScreen(vm)};composable("Quotes"){QuotesScreen(vm)};composable("Progress"){ProgressScreen(vm)};composable("Settings"){SettingsScreen(vm)}}}
    }
}
