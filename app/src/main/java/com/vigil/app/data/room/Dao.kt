package com.vigil.app.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Query("DELETE FROM check_ins")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: CheckInEntity)

    @Update
    suspend fun update(checkIn: CheckInEntity)

    @Delete
    suspend fun delete(checkIn: CheckInEntity)

    @Query("SELECT * FROM check_ins WHERE date = :date")
    suspend fun getByDate(date: Long): CheckInEntity?

    @Query("SELECT * FROM check_ins ORDER BY date DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<CheckInEntity>

    @Query("SELECT * FROM check_ins WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getByDateRange(startDate: Long, endDate: Long): List<CheckInEntity>

    @Query("SELECT * FROM check_ins ORDER BY date DESC")
    fun getAll(): Flow<List<CheckInEntity>>

    @Query("SELECT COUNT(*) FROM check_ins")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM check_ins WHERE hadRelapse = 1")
    suspend fun getRelapseCount(): Int

    @Query("SELECT MAX(date) FROM check_ins")
    suspend fun getLastCheckInDate(): Long?

    // Streak calculation
    @Query("""
        SELECT date FROM check_ins 
        WHERE hadRelapse = 0 
        ORDER BY date DESC
    """)
    suspend fun getStreakDates(): List<Long>
}

@Dao
interface GoalDao {
    @Query("DELETE FROM goals")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActiveGoals(): List<GoalEntity>

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    suspend fun getAllGoals(): List<GoalEntity>

    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveGoalsFlow(): Flow<List<GoalEntity>>
}

@Dao
interface MilestoneDao {
    @Query("DELETE FROM milestones")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milestone: MilestoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(milestones: List<MilestoneEntity>)

    @Update
    suspend fun update(milestone: MilestoneEntity)

    @Query("SELECT * FROM milestones ORDER BY requiredDays ASC")
    suspend fun getAll(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones WHERE isUnlocked = 1 ORDER BY unlockedDate DESC")
    suspend fun getUnlocked(): List<MilestoneEntity>

    @Query("SELECT * FROM milestones ORDER BY requiredDays ASC")
    fun getAllFlow(): Flow<List<MilestoneEntity>>
}

@Dao
interface BlockedDomainDao {
    @Query("DELETE FROM blocked_domains")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(domain: BlockedDomainEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(domains: List<BlockedDomainEntity>)

    @Update
    suspend fun update(domain: BlockedDomainEntity)

    @Delete
    suspend fun delete(domain: BlockedDomainEntity)

    @Query("SELECT * FROM blocked_domains WHERE domain = :domain")
    suspend fun getByDomain(domain: String): BlockedDomainEntity?

    @Query("SELECT * FROM blocked_domains WHERE type = :type AND isEnabled = 1")
    suspend fun getByType(type: String): List<BlockedDomainEntity>

    @Query("SELECT * FROM blocked_domains WHERE source = :source")
    suspend fun getBySource(source: String): List<BlockedDomainEntity>

    @Query("SELECT * FROM blocked_domains ORDER BY domain ASC")
    suspend fun getAll(): List<BlockedDomainEntity>

    @Query("SELECT * FROM blocked_domains ORDER BY domain ASC")
    fun getAllFlow(): Flow<List<BlockedDomainEntity>>

    @Query("DELETE FROM blocked_domains WHERE source = :source")
    suspend fun deleteBySource(source: String): Int

    @Query("UPDATE blocked_domains SET lastMatched = :timestamp WHERE domain = :domain")
    suspend fun updateLastMatched(domain: String, timestamp: Long)
}

@Dao
interface RestrictedAppDao {
    @Query("DELETE FROM restricted_apps")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(app: RestrictedAppEntity)

    @Update
    suspend fun update(app: RestrictedAppEntity)

    @Delete
    suspend fun delete(app: RestrictedAppEntity)

    @Query("SELECT * FROM restricted_apps WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): RestrictedAppEntity?

    @Query("SELECT * FROM restricted_apps WHERE isRestricted = 1")
    suspend fun getRestricted(): List<RestrictedAppEntity>

    @Query("SELECT * FROM restricted_apps ORDER BY appName ASC")
    suspend fun getAll(): List<RestrictedAppEntity>

    @Query("UPDATE restricted_apps SET lastBlocked = :timestamp WHERE packageName = :packageName")
    suspend fun updateLastBlocked(packageName: String, timestamp: Long)
}

@Dao
interface ProtectionEventDao {
    @Query("DELETE FROM protection_events")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ProtectionEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<ProtectionEventEntity>)

    @Query("SELECT * FROM protection_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<ProtectionEventEntity>

    @Query("SELECT * FROM protection_events WHERE eventType = :type ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getByType(type: String, limit: Int): List<ProtectionEventEntity>

    @Query("SELECT * FROM protection_events WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    suspend fun getByDateRange(start: Long, end: Long): List<ProtectionEventEntity>

    @Query("DELETE FROM protection_events WHERE timestamp < :cutoff")
    suspend fun deleteOldEvents(cutoff: Long): Int
}

@Dao
interface SettingsDao {
    @Query("DELETE FROM settings")
    suspend fun deleteAll()

    @Query("SELECT * FROM settings WHERE key = :key")
    suspend fun get(key: String): SettingsEntity?

    @Query("SELECT value FROM settings WHERE key = :key")
    suspend fun getValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: SettingsEntity)

    @Delete
    suspend fun delete(setting: SettingsEntity)

    @Query("DELETE FROM settings WHERE key = :key")
    suspend fun deleteByKey(key: String): Int
}
