package com.vigil.app.blocking

import android.content.Context
import com.vigil.app.data.VigilRepository
import com.vigil.app.domain.DomainMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Owns the set of blocked / allowed domains, keeps an in-memory lookup table
 * for the hot path (DNS packet handling), and refreshes it from remote lists.
 */
class BlocklistManager(
    private val context: Context,
    private val repository: VigilRepository
) {

    @Volatile private var blocked: Set<String> = emptySet()
    @Volatile private var allowed: Set<String> = emptySet()

    fun currentBlocked(): Set<String> = blocked
    fun currentAllowed(): Set<String> = allowed
    fun size(): Int = blocked.size

    /** Loads custom + stored lists into memory, seeding bundled defaults once. */
    suspend fun init() = withContext(Dispatchers.IO) {
        val stored = repository.blockedSet()
        if (stored.isEmpty()) {
            val seeded = DomainMatcher.parseList(readAsset("adult_domains.txt"))
            repository.replaceList("bundled", seeded.toList())
        }
        reload()
    }

    suspend fun reload() = withContext(Dispatchers.IO) {
        blocked = repository.blockedSet()
        allowed = repository.allowedSet()
    }

    fun isBlocked(host: String?): Boolean {
        if (host.isNullOrBlank()) return false
        return DomainMatcher.shouldBlock(host, blocked, allowed)
    }

    /**
     * Downloads a hosts/plain blocklist. Runs on IO, returns the number of
     * entries imported, or throws with a readable message.
     */
    suspend fun updateFromRemote(source: String, url: String): Int = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 20_000
        conn.readTimeout = 60_000
        conn.setRequestProperty("User-Agent", "VIGIL/1.0")
        try {
            if (conn.responseCode !in 200..299) {
                throw IllegalStateException("Server returned ${conn.responseCode}")
            }
            val text = conn.inputStream.bufferedReader().use { reader ->
                val sb = StringBuilder()
                val buf = CharArray(16 * 1024)
                var total = 0
                while (true) {
                    val n = reader.read(buf)
                    if (n <= 0) break
                    total += n
                    if (total > 40 * 1024 * 1024) break // 40 MB guard
                    sb.append(buf, 0, n)
                }
                sb.toString()
            }
            val parsed = DomainMatcher.parseList(text)
            if (parsed.isEmpty()) throw IllegalStateException("The list contained no valid domains")
            repository.replaceList(source, parsed.toList())
            reload()
            parsed.size
        } finally {
            conn.disconnect()
        }
    }

    fun bundledCount(): Int = DomainMatcher.parseList(readAsset("adult_domains.txt")).size

    private fun readAsset(name: String): String = try {
        context.assets.open(name).bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        ""
    }

    companion object {
        const val SOURCE_BUNDLED = "bundled"
        const val SOURCE_OISD = "oisd"
        const val SOURCE_CUSTOM = "custom"

        const val OISD_FULL = "https://raw.githubusercontent.com/oisd/full/main/domains.txt"
        const val HAGEZI_PRO = "https://raw.githubusercontent.com/hagezi/dns-blocklists/main/wildcard/pro.txt"
    }
}
