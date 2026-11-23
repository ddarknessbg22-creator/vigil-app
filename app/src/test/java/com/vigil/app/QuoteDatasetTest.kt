package com.vigil.app

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteDatasetTest {
    private val entries: List<Pair<String, String>> = File("src/main/assets/quotes").listFiles().orEmpty()
        .sortedBy { it.name }
        .flatMap { file ->
            val lines = file.readLines()
            lines.drop(1).flatMap { it.split('|') }.map { it.trim() }.filter { it.isNotEmpty() }.map { lines.first().trim() to it }
        }

    @Test
    fun hasAtLeastTwoThousandUniqueQuotes() {
        val unique = entries.map { it.second.lowercase() }.toSet()
        assertEquals(entries.size, unique.size)
        assertTrue("Only ${unique.size} quotes", unique.size >= 2000)
    }

    @Test
    fun coversAllTwentyCategories() {
        assertEquals(20, entries.map { it.first }.toSet().size)
        assertTrue(entries.any { it.first == "Morning motivation" })
        assertTrue(entries.any { it.first == "Night motivation" })
    }
}
