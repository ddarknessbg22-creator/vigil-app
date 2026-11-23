package com.vigil.app.data

import com.vigil.app.data.room.Run
import java.time.Duration

fun Run.elapsed(now:Long):Duration = Duration.ofMillis(((end?:now)-start).coerceAtLeast(0L))
val milestones=listOf(1L,3L,7L,14L,30L,60L,90L,180L,365L,730L,1000L)
