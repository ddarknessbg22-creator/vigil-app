package com.vigil.app
import com.vigil.app.data.*
import com.vigil.app.data.room.Run
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class StreakTest {
    @Test fun offlineIntervalDoesNotNeedCheckIns(){assertEquals(7L,Run(0).elapsed(7*86400000L).toDays())}
    @Test fun closedIntervalStopsAtSetback(){assertEquals(10L,Run(1000,601000).elapsed(90000000).toMinutes())}
    @Test fun clockRollbackCannotCreateNegativeTime(){assertEquals(Duration.ZERO,Run(2000).elapsed(0))}
    @Test fun daylightSavingIsNotAFreeDay(){val z=ZoneId.of("America/New_York");val t=LocalDate.of(2024,3,10).atStartOfDay(z);assertEquals(23L,Run(t.toInstant().toEpochMilli()).elapsed(t.plusDays(1).toInstant().toEpochMilli()).toHours())}
    @Test fun readingsAreStableByDay(){val lib=(0..9).map { Quote("q$it","Keep going","Line $it") };val t=LocalDateTime.of(2025,1,1,9,0);assertEquals(Quotes.featured(lib,t,"Any"),Quotes.featured(lib,t.plusHours(8),"Any"));assertNotEquals(Quotes.featured(lib,t,"Any"),Quotes.featured(lib,t.plusDays(1),"Any"));assertEquals(Quotes.featured(lib,t,"Keep going"),Quotes.featured(lib,t,"Any"))}
}
