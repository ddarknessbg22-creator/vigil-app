package com.vigil.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@Serializable
private data class GhAsset(val name: String, val size: Long = 0, val browser_download_url: String)

@Serializable
private data class GhRelease(
    val tag_name: String,
    val body: String? = null,
    val published_at: String? = null,
    val assets: List<GhAsset> = emptyList()
)

data class AppUpdate(
    val version: String,
    val notes: String,
    val size: String,
    val published: String,
    val url: String
)

/** Checks the project's GitHub releases for a newer APK. */
class UpdateChecker(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val repoOwner = "ddarknessbg22-creator"
    private val repoName = "vigil-clean-days"

    suspend fun latest(): AppUpdate? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                .openConnection() as HttpURLConnection
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.inputStream.use { it.bufferedReader().readText() }
                .let { json.decodeFromString<GhRelease>(it) }
        }.getOrNull()?.let { release ->
            val version = release.tag_name.trimStart('v', 'V')
            val apk = release.assets.firstOrNull { it.name.endsWith(".apk") }
                ?: return@let null
            if (isNewer(version)) {
                AppUpdate(
                    version = version,
                    notes = cleanNotes(release.body),
                    size = formatSize(apk.size),
                    published = release.published_at?.take(10)?.replace('-', '.') ?: "recently",
                    url = apk.browser_download_url
                )
            } else {
                null
            }
        }
    }

    /** Downloads the release APK into private storage, reporting progress. */
    suspend fun download(url: String, onProgress: (Long, Long) -> Unit = { _, _ -> }): File =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "apk").apply { mkdirs() }
            val target = File(dir, "update.apk")
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 60_000
            connection.instanceFollowRedirects = true
            try {
                val total = connection.contentLengthLong
                connection.inputStream.use { input ->
                    target.outputStream().use { output ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        var lastReported = 0L
                        while (true) {
                            val n = input.read(buffer)
                            if (n <= 0) break
                            output.write(buffer, 0, n)
                            done += n
                            // Throttle UI updates to every 128 KB.
                            if (done - lastReported >= 131_072) {
                                lastReported = done
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

    /** Turns GitHub release Markdown into plain, readable lines. */
    private fun cleanNotes(raw: String?): String {
        val cleaned = raw
            ?.lineSequence()
            .map { it
                .replace(Regex("^#+\\s*"), "")            // headings
                .replace("**", "")                          // bold
                .replace(Regex("^\\s*[-*]\\s+"), "• ")     // bullets
                .trim()
            }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .take(600)
            .orEmpty()
        return cleaned.ifBlank { "Stability improvements and new motivation lines." }
    }

    private fun formatSize(bytes: Long): String = when {
        bytes <= 0 -> "size unknown"
        bytes >= 1_048_576 -> String.format(Locale.US, "%.1f MB", bytes / 1_048_576.0)
        else -> String.format(Locale.US, "%d KB", bytes / 1024)
    }

    /** Manual check from Settings: reports even when there is no update. */
    suspend fun checkNow(): Result<AppUpdate?> = runCatching { latest() }

    private fun isNewer(candidate: String): Boolean {
        val parse = { value: String ->
            value.split('.').mapNotNull { it.toIntOrNull() }
        }
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
