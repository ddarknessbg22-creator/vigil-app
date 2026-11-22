package com.vigil.app.blocking.root

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Root-backed system-wide blocking through the hosts file.
 *
 * Superuser permission is requested through the platform "su" binary, which is
 * what Magisk, KernelSU and APatch all expose. Nothing is installed silently,
 * nothing persists behind the user's back: the hosts block is delimited by
 * markers so it can always be removed, and the app removes it on emergency
 * disable.
 */
class RootManager(private val context: Context) {

    enum class Status { UNKNOWN, AVAILABLE, DENIED, NOT_ROOTED }

    @Volatile
    private var cached: Status = Status.UNKNOWN

    fun isRootAvailable(): Boolean = exec("id").contains("uid=0")

    /** Asks for root once and remembers the answer for this session. */
    fun requestAccess(): Status {
        if (cached == Status.AVAILABLE) return cached
        cached = when {
            !isRootAvailable() -> Status.NOT_ROOTED
            !canWriteHosts() -> Status.DENIED
            else -> Status.AVAILABLE
        }
        return cached
    }

    fun status(): Status = cached

    private fun canWriteHosts(): Boolean {
        val probe = exec("su -c 'echo VIGIL_OK'")
        return probe.contains("VIGIL_OK")
    }

    /** Writes the blocklist into /system/etc/hosts between VIGIL markers. */
    fun applyHosts(domains: Collection<String>): Result<Int> {
        if (requestAccess() != Status.AVAILABLE) {
            return Result.failure(SecurityException("Root access was not granted"))
        }
        val list = domains.filter { it.isNotBlank() }.distinct()
        if (list.isEmpty()) return Result.failure(IllegalArgumentException("Blocklist is empty"))

        return try {
            val payload = list.joinToString("\n") { "0.0.0.0 $it\n0.0.0.0 www.$it" }
            val script = """
                mount -o rw,remount /system 2>/dev/null || mount -o rw,remount / 2>/dev/null
                H=/system/etc/hosts
                [ -f "${'$'}H" ] || H=/etc/hosts
                cp "${'$'}H" "${'$'}H.vigil.bak" 2>/dev/null
                sed -i '/# VIGIL START/,/# VIGIL END/d' "${'$'}H"
                printf '# VIGIL START\n%s\n# VIGIL END\n' "${payload.replace("\n", "\\n")}" >> "${'$'}H"
                echo VIGIL_WRITTEN
            """.trimIndent()

            val out = execScript(script)
            if (out.contains("VIGIL_WRITTEN")) Result.success(list.size)
            else Result.failure(IllegalStateException(out.take(400).ifBlank { "Root command failed" }))
        } catch (t: Throwable) {
            Log.e(TAG, "applyHosts failed", t)
            Result.failure(t)
        }
    }

    /** Removes the VIGIL block and restores the backup if one exists. */
    fun clearHosts(): Result<Unit> {
        if (requestAccess() != Status.AVAILABLE) {
            return Result.failure(SecurityException("Root access was not granted"))
        }
        val script = """
            H=/system/etc/hosts
            [ -f "${'$'}H" ] || H=/etc/hosts
            if [ -f "${'$'}H.vigil.bak" ]; then cp "${'$'}H.vigil.bak" "${'$'}H"; else sed -i '/# VIGIL START/,/# VIGIL END/d' "${'$'}H"; fi
            echo VIGIL_CLEARED
        """.trimIndent()
        return try {
            val out = execScript(script)
            if (out.contains("VIGIL_CLEARED")) Result.success(Unit)
            else Result.failure(IllegalStateException(out.take(400).ifBlank { "Root command failed" }))
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    /** Runs a raw shell command with root and returns combined output. */
    fun execScript(script: String): String {
        val safe = script.replace("'", "'\\''")
        return exec("su -c '$safe'")
    }

    private fun exec(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val out = StringBuilder()
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    out.append(line).append('\n')
                    line = reader.readLine()
                }
            }
            process.waitFor(15, TimeUnit.SECONDS)
            out.toString()
        } catch (t: Throwable) {
            Log.w(TAG, "exec failed: $command", t)
            ""
        }
    }

    /** True when the hosts file currently contains our markers. */
    fun isHostsActive(): Boolean =
        requestAccess() == Status.AVAILABLE &&
            execScript("grep -c 'VIGIL START' /system/etc/hosts 2>/dev/null || echo 0").trim().let {
                it.isNotEmpty() && it != "0"
            }

    fun hostsFile(): File = File("/system/etc/hosts")

    companion object {
        private const val TAG = "RootManager"
    }
}
