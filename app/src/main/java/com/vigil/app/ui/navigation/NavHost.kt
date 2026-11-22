package com.vigil.app.ui.navigation

import androidx.compose.material3.NavHost
import androidx.compose.material3.composable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
