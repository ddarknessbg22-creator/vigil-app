package com.vigil.app.data

import com.vigil.app.data.room.Entry
import com.vigil.app.data.room.Run
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Run.elapsed(now: Long): Duration = Duration.ofMillis(((end ?: now) - start).coerceAtLeast(0L))

/**
 * Streak from actual daily decisions, not from elapsed time.
 *
 * Counting from a start timestamp meant the number only moved when the date
 * rolled over — checking in did nothing visible. Here every clean day is one
 * you actually logged as passed.
 *
 * Before today's check-in window, the streak holds at yesterday's value so it
 * never drops to 0 just because a new date began.
 */
fun cleanStreak(
    entries: List<Entry>,
    today: LocalDate = LocalDate.now()
): Long {
    val passed = entries
        .filter { !it.setback }
        .mapNotNull { runCatching { LocalDate.parse(it.day) }.getOrNull() }
        .toSet()
    if (passed.isEmpty()) return 0L

    // Anchor on the most recent clean day that is not in the future.
    val anchor = passed.filter { !it.isAfter(today) }.maxOrNull()
        ?: return 0L
    var cursor = anchor
    var count = 0L
    while (passed.contains(cursor)) {
        count++
        cursor = cursor.minusDays(1)
    }
    return count
}

/** Longest sequence of consecutive clean days ever logged. */
fun bestStreak(entries: List<Entry>): Long {
    val passed = entries
        .filter { !it.setback }
        .mapNotNull { runCatching { LocalDate.parse(it.day) }.getOrNull() }
        .sorted()
    if (passed.isEmpty()) return 0L

    var best = 1L
    var run = 1L
    for (i in 1 until passed.size) {
        run = if (ChronoUnit.DAYS.between(passed[i - 1], passed[i]) == 1L) run + 1 else 1L
        if (run > best) best = run
    }
    return best
}

/** Last valid millisecond before a setback day — the old run stops here. */
fun endBeforeSetback(setbackDate: LocalDate): Long =
    setbackDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

/** A new run begins the day after a setback; a failed day never counts. */
fun nextRunStart(setbackDate: LocalDate): Long =
    setbackDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

/** Legacy calendar-day helper kept for history views and backups. */
fun Run.streakDays(now: Long = System.currentTimeMillis()): Long {
    val zone = ZoneId.systemDefault()
    val from = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
    val until = Instant.ofEpochMilli(end ?: now).atZone(zone).toLocalDate()
    if (until.isBefore(from)) return 0L
    return ChronoUnit.DAYS.between(from, until) + 1
}

val milestones = listOf(1L, 3L, 7L, 14L, 30L, 60L, 90L, 180L, 365L, 730L, 1000L)
