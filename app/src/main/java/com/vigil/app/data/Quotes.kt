package com.vigil.app.data

import android.content.Context
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Quote(val id: String, val category: String, val text: String, val source: String = "")

/**
 * Motivation lines written for one purpose: staying clean.
 *
 * Asset format: first line = category, remaining lines are `|`-separated
 * quotes. Optional ` ~~ source` suffix adds an attribution.
 */
object Quotes {
    private const val PREF = "stay-clean"

    fun load(c: Context): List<Quote> = buildList {
        for (file in c.assets.list("quotes").orEmpty().sorted()) {
            val lines = c.assets.open("quotes/$file").bufferedReader().use { it.readLines() }
            if (lines.isEmpty()) continue
            val category = lines.first().trim()
            lines.drop(1).flatMap { it.split('|') }.map { it.trim() }.filter { it.isNotEmpty() }.forEach { raw ->
                val parts = raw.split(" ~~ ", limit = 2)
                val text = parts[0].trim()
                val source = if (parts.size > 1) parts[1].trim() else ""
                add(Quote(UUID.nameUUIDFromBytes(raw.toByteArray()).toString(), category, text, source))
            }
        }
    }

    /**
     * One line per day, stable across the day and across restarts.
     * Evening and morning draw from the same pool so the message fits the
     * moment without being a different "product" at night.
     */
    fun featured(all: List<Quote>, time: LocalDateTime, preference: String = "Any"): Quote? {
        if (all.isEmpty()) return null
        val pool = if (preference != "Any") {
            all.filter { it.category == preference }.ifEmpty { all }
        } else {
            all
        }
        val day = time.minusHours(5).toLocalDate().toEpochDay()
        return pool[Math.floorMod(day, pool.size.toLong()).toInt()]
    }
}
