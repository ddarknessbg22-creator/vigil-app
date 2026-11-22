package com.vigil.app.data.room.converters

import androidx.room.TypeConverter
import java.time.Instant

object Converters {
    @TypeConverter
    fun fromInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun toInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }
}
