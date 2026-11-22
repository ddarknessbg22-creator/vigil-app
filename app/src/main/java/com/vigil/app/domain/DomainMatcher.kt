package com.vigil.app.domain

import java.net.IDN

/**
 * Pure-Kotlin domain matching shared by the DNS filter, the root hosts writer
 * and the in-app blocking tests. No Android dependencies, so it is unit tested.
 */
object DomainMatcher {

    /** Removes scheme, path, port, leading dots and lowercases. */
    fun normalize(raw: String): String {
        var value = raw.trim().lowercase()
        if (value.isEmpty()) return ""
        value = value.substringBefore('#').trim()
        val schemeIdx = value.indexOf("://")
        if (schemeIdx >= 0) value = value.substring(schemeIdx + 3)
        value = value.substringBefore('/').substringBefore('?')
        value = value.substringBefore(':')
        value = value.trim('.')
        if (value.isEmpty()) return ""
        return try {
            IDN.toASCII(value)
        } catch (_: Exception) {
            value
        }
    }

    fun isValidDomain(raw: String): Boolean {
        val d = normalize(raw)
        if (d.isEmpty() || d.length > 253) return false
        if (!d.contains('.')) return false
        val labels = d.split('.')
        if (labels.any { it.isEmpty() || it.length > 63 }) return false
        val tld = labels.last()
        if (tld.length < 2 || tld.any { !it.isLetter() }) return false
        return labels.all { label ->
            label.all { c -> c.isLetterOrDigit() || c == '-' } &&
                !label.startsWith('-') && !label.endsWith('-')
        }
    }

    /**
     * A domain is blocked when it, or any parent label, appears in [blocked],
     * unless a parent label is explicitly allowlisted (allowlist wins).
     */
    fun shouldBlock(
        domain: String,
        blocked: Set<String>,
        allowed: Set<String> = emptySet()
    ): Boolean {
        val d = normalize(domain)
        if (d.isEmpty()) return false
        val candidates = suffixes(d)

        if (candidates.any { allowed.contains(it) }) return false
        return candidates.any { blocked.contains(it) }
    }

    /** example.a.b.com -> [example.a.b.com, a.b.com, b.com] */
    fun suffixes(domain: String): List<String> {
        val parts = normalize(domain).split('.')
        if (parts.size < 2) return emptyList()
        return (0 until parts.size - 1).map { parts.subList(it, parts.size).joinToString(".") }
    }

    /** Parses a hosts-format or plain-domain list into a set of domains. */
    fun parseList(text: String): Set<String> {
        val out = HashSet<String>(text.length / 24)
        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("!")) return@forEach
            var candidate = trimmed
            if (candidate.startsWith("0.0.0.0 ") || candidate.startsWith("127.0.0.1 ")) {
                candidate = candidate.substringAfter(' ')
            }
            if (candidate.contains(' ')) candidate = candidate.substringBefore(' ')
            val domain = normalize(candidate)
            if (isValidDomain(domain)) out.add(domain)
        }
        return out
    }
}
