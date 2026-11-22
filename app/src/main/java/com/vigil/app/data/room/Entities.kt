package com.vigil.app.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.vigil.app.data.room.converters.Converters
import java.time.Instant
import java.util.UUID

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = Instant.now().toEpochMilli(), // Date at midnight UTC
    val mood: Int = 3, // 1-5 scale
    val notes: String = "",
    val hadUrge: Boolean = false,
    val hadRelapse: Boolean = false,
    val createdAt: Long = Instant.now().toEpochMilli()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val targetDays: Int,
    val startDate: Long = Instant.now().toEpochMilli(),
    val endDate: Long = 0, // Calculated from startDate + targetDays
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val completedDate: Long = 0,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val updatedAt: Long = Instant.now().toEpochMilli()
)

@Entity(tableName = "milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String, // Icon name/resource
    val color: Int, // Color int
    val requiredDays: Int,
    val isUnlocked: Boolean = false,
    val unlockedDate: Long = 0,
    val order: Int
)

@Entity(tableName = "blocked_domains")
data class BlockedDomainEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val domain: String,
    val type: String, // "blocked" or "allowed"
    val source: String, // "custom", "oisd", "hagezi", etc.
    val isEnabled: Boolean = true,
    val addedAt: Long = Instant.now().toEpochMilli(),
    val lastMatched: Long = 0
)

@Entity(tableName = "restricted_apps")
data class RestrictedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isRestricted: Boolean = true,
    val restrictionType: String = "block", // "block", "time_limit", "schedule"
    val maxDailyMinutes: Int = 0,
    val addedAt: Long = Instant.now().toEpochMilli(),
    val lastBlocked: Long = 0
)

@Entity(tableName = "protection_events")
data class ProtectionEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val eventType: String, // "blocked", "allowed", "protection_enabled", "protection_disabled", "test_run"
    val domain: String = "",
    val packageName: String = "",
    val details: String = "",
    val timestamp: Long = Instant.now().toEpochMilli()
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
