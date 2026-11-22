package com.vigil.app.blocking.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequest
import com.vigil.app.blocking.worker.BlocklistUpdateWorker
import com.vigil.app.blocking.worker.ProtectionMonitorWorker

class BlockingBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "BlockingReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "Boot completed - scheduling protection restoration")
                scheduleProtectionRestore(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "App updated - reinitializing protection")
                scheduleProtectionRestore(context)
            }
            else -> {
                Log.w(TAG, "Unknown intent action: ${intent.action}")
            }
        }
    }

    private fun scheduleProtectionRestore(context: Context) {
        // Restore VPN service
        val vpnIntent = Intent(context, com.vigil.app.blocking.vpn.VigilVpnService::class.java)
        context.startForegroundService(vpnIntent)

        // Schedule blocklist update check
        val updateWork = OneTimeWorkRequest.Builder(BlocklistUpdateWorker::class.java)
            .setInitialDelay(5, java.util.concurrent.TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueue(updateWork)

        // Schedule protection monitor
        val monitorWork = OneTimeWorkRequest.Builder(ProtectionMonitorWorker::class.java)
            .setInitialDelay(1, java.util.concurrent.TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueue(monitorWork)
    }
}
