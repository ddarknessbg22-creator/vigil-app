package com.vigil.app.data.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao interface JournalDao {
    @Query("SELECT * FROM journal ORDER BY day DESC, id DESC") fun entries():Flow<List<Entry>>
    @Query("SELECT * FROM intentions ORDER BY complete,title") fun goals():Flow<List<Goal>>
    @Query("SELECT * FROM runs ORDER BY start DESC") fun runs():Flow<List<Run>>
    @Query("SELECT * FROM runs ORDER BY start DESC") suspend fun runsOnce():List<Run>
    @Query("SELECT * FROM reading ORDER BY seen DESC") fun readings():Flow<List<Reading>>
    @Query("SELECT * FROM reading WHERE id=:id") suspend fun reading(id:String):Reading?
    @Query("SELECT * FROM runs ORDER BY start DESC LIMIT 1") suspend fun latest():Run?
    @Query("SELECT * FROM journal WHERE day=:day LIMIT 1") suspend fun entry(day:String):Entry?
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(value:Entry)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(value:Goal)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(value:Run)
    @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(value:Reading)
    @Query("DELETE FROM runs WHERE start=:start") suspend fun removeRun(start:Long)
    @Query("DELETE FROM journal") suspend fun clearEntries()
    @Query("DELETE FROM intentions") suspend fun clearGoals()
    @Query("DELETE FROM runs") suspend fun clearRuns()
    @Query("DELETE FROM reading") suspend fun clearReadings()
}
