package com.vigil.app.blocking.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.concurrent.TimeUnit

class BlocklistUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "BlocklistUpdateWorker"

    companion object {
        const val OISD_FULL_URL = "https://raw.githubusercontent.com/oisd/full/main/domains.txt"
        const val OISD_BASIC_URL = "https://raw.githubusercontent.com/oisd/basic/main/domains.txt"
        const val HAGEZI_PRO_URL = "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/wildcard/pro.txt"
    }

    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                val selectedList = getSelectedBlocklist()
                val url = when (selectedList) {
                    "oisd_full" -> OISD_FULL_URL
                    "oisd_basic" -> OISD_BASIC_URL
                    "hagezi_pro" -> HAGEZI_PRO_URL
                    else -> OISD_FULL_URL
                }

                val domains = downloadBlocklist(url)
                if (domains.isNotEmpty()) {
                    saveBlocklist(domains, selectedList)
                    logEvent("blocklist_updated", "Updated $selectedList with ${domains.size} domains")
                    Result.success()
                } else {
                    logEvent("blocklist_update_failed", "No domains downloaded")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Blocklist update failed", e)
            logEvent("blocklist_update_error", e.message ?: "Unknown error")
            Result.retry()
        }
    }

    private fun getSelectedBlocklist(): String {
        // In real implementation, read from DataStore
        return "oisd_full"
    }

    private fun downloadBlocklist(urlString: String): List<String> {
        val domains = mutableListOf<String>()
        try {
            val url = URL(urlString)
            val connection = url.openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            BufferedReader(InputStreamReader(connection.getInputStream())).use { reader ->
                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        domains.add(trimmed.lowercase())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Download failed for $urlString", e)
        }
        return domains
    }

    private fun saveBlocklist(domains: List<String>, source: String) {
        // In real implementation, save to Room database
        // This would be done via a repository injected through Hilt
        Log.i(TAG, "Saving ${domains.size} domains from $source")
    }

    private fun logEvent(type: String, details: String) {
        // Log to protection events table
        Log.i(TAG, "$type: $details")
    }
}
