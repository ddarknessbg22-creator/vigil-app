package com.vigil.app.data

import com.vigil.app.data.room.Run
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Run.elapsed(now: Long): Duration = Duration.ofMillis(((end ?: now) - start).coerceAtLeast(0L))

/**
 * Calendar days clean, inclusive of the last day.
 * A run that ended on a setback date is passed an `end` already moved to the
 * day before, so a failed day is never counted as clean.
 */
fun Run.streakDays(now: Long = System.currentTimeMillis()): Long {
    val zone = ZoneId.systemDefault()
    val from = Instant.ofEpochMilli(start).atZone(zone).toLocalDate()
    val until = Instant.ofEpochMilli(end ?: now).atZone(zone).toLocalDate()
    if (until.isBefore(from)) return 0L
    return ChronoUnit.DAYS.between(from, until) + 1
}

/** Last valid millisecond before a setback day — the old run stops here. */
fun endBeforeSetback(setbackDate: LocalDate): Long =
    setbackDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

/**
 * A new run begins at the start of the day AFTER a setback.
 * Until that date arrives, `streakDays` correctly reports 0 — the failed day
 * is never day one of a new streak.
 */
fun nextRunStart(setbackDate: LocalDate): Long =
    setbackDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

val milestones = listOf(1L, 3L, 7L, 14L, 30L, 60L, 90L, 180L, 365L, 730L, 1000L)
