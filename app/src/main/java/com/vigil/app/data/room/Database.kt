package com.vigil.app.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vigil.app.data.room.converters.Converters

@Database(
    entities = [
        CheckInEntity::class,
        GoalEntity::class,
        MilestoneEntity::class,
        BlockedDomainEntity::class,
        RestrictedAppEntity::class,
        ProtectionEventEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class VigilDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun goalDao(): GoalDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun blockedDomainDao(): BlockedDomainDao
    abstract fun restrictedAppDao(): RestrictedAppDao
    abstract fun protectionEventDao(): ProtectionEventDao
    abstract fun settingsDao(): SettingsDao

    /** Wipes every table (used by emergency data deletion). */
    suspend fun clearAllTables() {
        withTransaction {
            checkInDao().deleteAll()
            goalDao().deleteAll()
            milestoneDao().deleteAll()
            blockedDomainDao().deleteAll()
            restrictedAppDao().deleteAll()
            protectionEventDao().deleteAll()
            settingsDao().deleteAll()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: VigilDatabase? = null

        fun getInstance(context: Context): VigilDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VigilDatabase::class.java,
                    "vigil_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
