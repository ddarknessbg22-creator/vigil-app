package com.vigil.app.data

import com.vigil.app.data.room.Run
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Run.elapsed(now: Long): Duration = Duration.ofMillis(((end ?: now) - start).coerceAtLeast(0L))

/**
 * The number shown to the user: calendar days clean, inclusive of today.
 *
 * A run started at 11pm yesterday is already a 2-day streak the next morning.
 * Counting elapsed 24-hour blocks instead made the app show 0 for a whole day,
 * which felt like losing progress. Count dates, not milliseconds.
 */
fun Run.streakDays(now: Long = System.currentTimeMillis()): Long {
    val zone = ZoneId.systemDefault()
    val from = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
    val until = Instant.ofEpochMilli(end ?: now).atZone(zone).toLocalDate()
    if (until.isBefore(from)) return 0L
    return ChronoUnit.DAYS.between(from, until) + 1
}

val milestones = listOf(1L, 3L, 7L, 14L, 30L, 60L, 90L, 180L, 365L, 730L, 1000L)
