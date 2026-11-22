package com.vigil.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vigil.app.ui.home.HomeScreen
import com.vigil.app.ui.protection.ProtectionScreen
import com.vigil.app.ui.progress.ProgressScreen
import com.vigil.app.ui.settings.SettingsScreen

@Composable
fun NavHost() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(onNavigateToProtection = { navController.navigate("protection") })
        }
        composable("protection") {
            ProtectionScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("progress") {
            ProgressScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}

// Scaffold state for bottom navigation
data class ScaffoldState(
    var currentTab: Int = 0
)
