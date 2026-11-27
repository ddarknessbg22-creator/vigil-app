package com.vigil.app
import com.vigil.app.data.*
import com.vigil.app.data.room.Run
import com.vigil.app.data.room.Entry
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class StreakTest {
    @Test fun offlineIntervalDoesNotNeedCheckIns(){assertEquals(7L,Run(0).elapsed(7*86400000L).toDays())}
    @Test fun closedIntervalStopsAtSetback(){assertEquals(10L,Run(1000,601000).elapsed(90000000).toMinutes())}
    @Test fun clockRollbackCannotCreateNegativeTime(){assertEquals(Duration.ZERO,Run(2000).elapsed(0))}
    @Test fun daylightSavingIsNotAFreeDay(){val z=ZoneId.of("America/New_York");val t=LocalDate.of(2024,3,10).atStartOfDay(z);assertEquals(23L,Run(t.toInstant().toEpochMilli()).elapsed(t.plusDays(1).toInstant().toEpochMilli()).toHours())}
    @Test fun readingsAreStableByDay(){val lib=(0..9).map { Quote("q$it","Keep going","Line $it") };val t=LocalDateTime.of(2025,1,1,9,0);assertEquals(Quotes.featured(lib,t,"Any"),Quotes.featured(lib,t.plusHours(8),"Any"));assertNotEquals(Quotes.featured(lib,t,"Any"),Quotes.featured(lib,t.plusDays(1),"Any"));assertEquals(Quotes.featured(lib,t,"Keep going"),Quotes.featured(lib,t,"Any"))}

    @Test fun firstDayCountsAsOneImmediately(){
        val z=ZoneId.systemDefault()
        val start=LocalDate.now(z).atTime(23,0).atZone(z).toInstant().toEpochMilli()
        val now=LocalDate.now(z).atTime(23,30).atZone(z).toInstant().toEpochMilli()
        assertEquals(1L, Run(start).streakDays(now))
    }
    @Test fun streakCountsCalendarDaysNotElapsedBlocks(){
        val z=ZoneId.systemDefault()
        val start=LocalDate.now(z).minusDays(2).atTime(23,0).atZone(z).toInstant().toEpochMilli()
        val now=LocalDate.now(z).minusDays(1).atTime(9,0).atZone(z).toInstant().toEpochMilli()
        // Less than 24h elapsed, but two calendar dates clean.
        assertEquals(2L, Run(start).streakDays(now))
    }
    @Test fun bestNeverBelowOneForAValidRun(){
        val z=ZoneId.systemDefault()
        val start=LocalDate.now(z).atStartOfDay(z).toInstant().toEpochMilli()
        assertEquals(1L, Run(start).streakDays(start + 60000))
    }
    @Test fun failedDayNeverCountsAsStreak(){
        val z=ZoneId.systemDefault()
        val today=LocalDate.now(z)
        val threeDaysAgo=today.minusDays(3).atStartOfDay(z).toInstant().toEpochMilli()
        val old=Run(threeDaysAgo, endBeforeSetback(today))
        // Four calendar dates elapsed, but the setback day is excluded -> 3.
        assertEquals(3L, old.streakDays(today.atTime(23,0).atZone(z).toInstant().toEpochMilli()))
        // New run begins tomorrow: today reports 0, tomorrow reports 1.
        val fresh=Run(nextRunStart(today))
        assertEquals(0L, fresh.streakDays(today.atTime(23,0).atZone(z).toInstant().toEpochMilli()))
        assertEquals(1L, fresh.streakDays(today.plusDays(1).atTime(9,0).atZone(z).toInstant().toEpochMilli()))
    }

    private fun passed(day: LocalDate) = Entry(day.toString(), day.toString(), "", setback = false)

    @Test fun checkInMovesStreakImmediately(){
        val today=LocalDate.now()
        val yesterday=today.minusDays(1)
        // Only yesterday logged: holds at 1 until today's decision.
        assertEquals(1L, cleanStreak(listOf(passed(yesterday)), today))
        // The moment today is logged clean, it becomes 2 — no clock waiting.
        assertEquals(2L, cleanStreak(listOf(passed(yesterday), passed(today)), today))
    }
    @Test fun streakNeverAdvancesWithoutACheckIn(){
        val today=LocalDate.now()
        val two=today.minusDays(2)
        val one=today.minusDays(1)
        assertEquals(2L, cleanStreak(listOf(passed(two), passed(one)), today))
        assertEquals(2L, cleanStreak(listOf(passed(two), passed(one)), today.plusDays(1)))
    }
    @Test fun setbackStopsTheRunAndBestIsPreserved(){
        val today=LocalDate.now()
        val older=(1..4).map { passed(today.minusDays(it.toLong())) }
        val setback=Entry(today.minusDays(1).toString(), today.minusDays(1).toString(), "", setback=true)
        val entries=older.filter { it.day != setback.day } + setback
        // The run ended at the setback; the counter holds at the last clean run.
        assertEquals(3L, cleanStreak(entries, today))
        assertEquals(3L, bestStreak(older))
    }
}
