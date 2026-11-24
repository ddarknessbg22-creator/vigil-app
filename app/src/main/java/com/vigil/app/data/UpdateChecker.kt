package com.vigil.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class AppUpdate(
    val version: String,
    val notes: String,
    val size: String,
    val published: String,
    val url: String
)

/** Outcome of a manual or automatic update check. */
sealed class UpdateResult {
    data class Available(val update: AppUpdate) : UpdateResult()
    data object UpToDate : UpdateResult()
    data class Failed(val reason: String) : UpdateResult()
}

/**
 * Checks the project's GitHub releases for a newer APK.
 *
 * Errors are surfaced explicitly — they must never be reported as "up to date".
 */
class UpdateChecker(private val context: Context) {

    private val repoOwner = "ddarknessbg22-creator"
    private val repoName = "vigil-app"

    suspend fun check(): UpdateResult = withContext(Dispatchers.IO) {
        val text = try {
            val connection = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                .openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", "VIGIL-Android")
            try {
                val code = connection.responseCode
                if (code !in 200..299) {
                    return@withContext UpdateResult.Failed("GitHub returned $code")
                }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            return@withContext UpdateResult.Failed("No connection to GitHub")
        }

        val release = try {
            JSONObject(text)
        } catch (e: Exception) {
            return@withContext UpdateResult.Failed("Could not read the response")
        }

        val tag = release.optString("tag_name", "")
        val version = tag.trimStart('v', 'V')
        if (version.isBlank()) return@withContext UpdateResult.Failed("Release has no version")

        if (!isNewer(version)) return@withContext UpdateResult.UpToDate

        val assets = release.optJSONArray("assets")
        var apkName = ""
        var apkUrl = ""
        var apkSize = 0L
        if (assets != null) {
            for (i in 0 until assets.length()) {
                val a = assets.optJSONObject(i) ?: continue
                val name = a.optString("name", "")
                if (name.endsWith(".apk")) {
                    apkName = name
                    apkUrl = a.optString("browser_download_url", "")
                    apkSize = a.optLong("size", 0L)
                    break
                }
            }
        }
        if (apkUrl.isBlank()) return@withContext UpdateResult.Failed("Release has no APK")

        UpdateResult.Available(
            AppUpdate(
                version = version,
                notes = cleanNotes(release.optString("body", "")),
                size = formatSize(apkSize),
                published = release.optString("published_at", "").take(10).replace('-', '.'),
                url = apkUrl
            )
        )
    }

    /** Downloads the release APK into private storage, reporting progress. */
    suspend fun download(url: String, onProgress: (Long, Long) -> Unit = { _, _ -> }): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "apk").apply { mkdirs() }
            val target = File(dir, "update.apk")
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 20_000
            connection.readTimeout = 90_000
            connection.instanceFollowRedirects = true
            try {
                val total = connection.contentLengthLong
                connection.inputStream.use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        var last = 0L
                        while (true) {
                            val n = input.read(buffer)
                            if (n <= 0) break
                            output.write(buffer, 0, n)
                            done += n
                            if (done - last >= 131_072) {
                                last = done
                                onProgress(done, total)
                            }
                        }
                        onProgress(done, if (total > 0) total else done)
                    }
                }
            } finally {
                connection.disconnect()
            }
            target
        }

    fun installIntent(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun cleanNotes(raw: String): String {
        val cleaned = raw.lineSequence()
            .map {
                it.replace(Regex("^#+\\s*"), "")
                    .replace("**", "")
                    .replace(Regex("^\\s*[-*]\\s+"), "• ")
                    .replace(Regex("\\[([^]]+)]\\([^)]+\\)"), "$1")
                    .trim()
            }
            .filter { it.isNotBlank() && it != "What's new" && it != "Updating" }
            .joinToString("\n")
            .take(700)
        return cleaned.ifBlank { "Stability improvements and new motivation lines." }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes <= 0 -> "size unknown"
        bytes >= 1_048_576 -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576.0)
        else -> String.format(Locale.US, "%d KB", bytes / 1024)
    }

    private fun isNewer(candidate: String): Boolean {
        val parse = { value: String -> value.split('.').mapNotNull { it.toIntOrNull() } }
        val current = parse(com.vigil.app.BuildConfig.VERSION_NAME)
        val next = parse(candidate)
        for (i in 0 until maxOf(current.size, next.size)) {
            val a = current.getOrElse(i) { 0 }
            val b = next.getOrElse(i) { 0 }
            if (b > a) return true
            if (b < a) return false
        }
        return false
    }
}
