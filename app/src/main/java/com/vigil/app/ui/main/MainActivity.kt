package com.vigil.app.ui.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vigil.app.ui.navigation.VigilApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        useMaxRefreshRate()
        setContent { VigilApp() }
    }

    /** Ask the window for the display's highest available refresh rate. */
    @Suppress("DEPRECATION")
    private fun useMaxRefreshRate() {
        val display = window.windowManager.defaultDisplay ?: return
        val modes = display.supportedModes
        if (modes.isEmpty()) return

        val max = modes.maxOf { it.refreshRate }
        val rates = modes.map { it.refreshRate }.distinct().toFloatArray()
        val params: WindowManager.LayoutParams = window.attributes
        params.preferredRefreshRate = max
        params.supportedRefreshRates = rates
        window.attributes = params
    }
}
