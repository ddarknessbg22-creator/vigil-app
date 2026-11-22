package com.vigil.app.data

import androidx.room.withTransaction
import com.vigil.app.data.datastore.PrefKeys
import com.vigil.app.data.datastore.PreferencesDataStore
import com.vigil.app.data.room.BlockedDomainEntity
import com.vigil.app.data.room.CheckInEntity
import com.vigil.app.data.room.GoalEntity
import com.vigil.app.data.room.ProtectionEventEntity
import com.vigil.app.data.room.RestrictedAppEntity
import com.vigil.app.data.room.VigilDatabase
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class VigilRepository(
    private val db: VigilDatabase,
    val prefs: PreferencesDataStore
) {
    private val checkIns = db.checkInDao()
    private val goals = db.goalDao()
    private val milestones = db.milestoneDao()
    private val domains = db.blockedDomainDao()
    private val apps = db.restrictedAppDao()
    private val events = db.protectionEventDao()

    // ---------------- Habit ----------------

    fun checkInFlow(): Flow<List<CheckInEntity>> = checkIns.getAll()

    suspend fun todayCheckIn(): CheckInEntity? =
        checkIns.getByDate(todayStartMillis())

    suspend fun saveCheckIn(mood: Int, notes: String, hadUrge: Boolean, hadRelapse: Boolean) {
        val existing = checkIns.getByDate(todayStartMillis())
        val entity = (existing ?: CheckInEntity(date = todayStartMillis())).copy(
            mood = mood.coerceIn(1, 5),
            notes = notes,
            hadUrge = hadUrge,
            hadRelapse = hadRelapse
        )
        checkIns.insert(entity)
        if (hadRelapse) {
            events.insert(
                ProtectionEventEntity(
                    eventType = "relapse",
                    details = notes.take(120)
                )
            )
        }
    }

    suspend fun streakDays(): Int {
        val dates = checkIns.getStreakDates().map { toLocalDate(it) }.toSortedSet(compareByDescending { it })
        if (dates.isEmpty()) return 0
        val today = LocalDate.now()
        var cursor = if (dates.contains(today)) today else today.minusDays(1)
        if (!dates.contains(cursor)) return 0
        var streak = 0
        while (dates.contains(cursor)) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }

    suspend fun longestStreak(): Int {
        val dates = checkIns.getStreakDates().map { toLocalDate(it) }.toSortedSet()
        if (dates.isEmpty()) return 0
        var best = 1
        var run = 1
        val list = dates.toList()
        for (i in 1 until list.size) {
            run = if (list[i].toEpochDay() - list[i - 1].toEpochDay() == 1L) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    suspend fun recent(limit: Int = 30): List<CheckInEntity> = checkIns.getRecent(limit)

    suspend fun totals(): Totals {
        val relapseCount = checkIns.getRelapseCount()
        val checkInCount = checkIns.getTotalCount()
        val blocked = events.getByType("blocked", 10_000).size
        return Totals(checkIns = checkInCount, relapses = relapseCount, blocked = blocked)
    }

    data class Totals(val checkIns: Int, val relapses: Int, val blocked: Int)

    // ---------------- Goals ----------------

    fun activeGoals(): Flow<List<GoalEntity>> = db.goalDao().getActiveGoalsFlow()

    suspend fun createGoal(title: String, description: String, targetDays: Int) {
        goals.insert(
            GoalEntity(
                title = title.trim(),
                description = description.trim(),
                targetDays = targetDays.coerceIn(1, 3650),
                endDate = Instant.now().plusSeconds(targetDays * 86_400L).toEpochMilli()
            )
        )
    }

    suspend fun updateGoal(goal: GoalEntity) = goals.update(goal)

    suspend fun deleteGoal(goal: GoalEntity) = goals.delete(goal)

    suspend fun goalProgress(goal: GoalEntity): Float {
        val elapsed = ((System.currentTimeMillis() - goal.startDate) / 86_400_000.0).toInt()
        return (elapsed.toFloat() / goal.targetDays).coerceIn(0f, 1f)
    }

    // ---------------- Blocking ----------------

    fun domainsFlow(): Flow<List<BlockedDomainEntity>> = domains.getAllFlow()

    suspend fun blockedSet(): Set<String> =
        domains.getByType("blocked").map { it.domain }.toSet()

    suspend fun allowedSet(): Set<String> =
        domains.getByType("allowed").map { it.domain }.toSet()

    suspend fun addDomain(raw: String, type: String): Result<String> {
        val domain = com.vigil.app.domain.DomainMatcher.normalize(raw)
        if (!com.vigil.app.domain.DomainMatcher.isValidDomain(domain)) {
            return Result.failure(IllegalArgumentException("That is not a valid domain, e.g. example.com"))
        }
        domains.insert(BlockedDomainEntity(domain = domain, type = type, source = "custom"))
        return Result.success(domain)
    }

    suspend fun removeDomain(domain: String) {
        domains.getByDomain(domain)?.let { domains.delete(it) }
    }

    suspend fun replaceList(source: String, list: List<String>) {
        db.withTransaction {
            domains.deleteBySource(source)
            list.chunked(2000).forEach { chunk ->
                domains.insertAll(
                    chunk.map { BlockedDomainEntity(domain = it, type = "blocked", source = source) }
                )
            }
        }
    }

    suspend fun logBlocked(domain: String) {
        events.insert(ProtectionEventEntity(eventType = "blocked", domain = domain))
        domains.updateLastMatched(domain, System.currentTimeMillis())
    }

    suspend fun logEvent(type: String, details: String) {
        events.insert(ProtectionEventEntity(eventType = type, details = details))
    }

    suspend fun recentBlocked(limit: Int = 50): List<ProtectionEventEntity> =
        events.getByType("blocked", limit)

    // ---------------- Restricted apps ----------------

    suspend fun restrictedApps(): List<RestrictedAppEntity> = apps.getAll()

    suspend fun setAppRestriction(packageName: String, appName: String, restricted: Boolean) {
        apps.insert(
            RestrictedAppEntity(
                packageName = packageName,
                appName = appName,
                isRestricted = restricted
            )
        )
    }

    suspend fun removeApp(packageName: String) {
        apps.getByPackageName(packageName)?.let { apps.delete(it) }
    }

    // ---------------- Maintenance ----------------

    suspend fun purgeOldEvents() {
        events.deleteOldEvents(System.currentTimeMillis() - 90L * 86_400_000)
    }

    suspend fun wipeEverything() {
        db.withTransaction {
            checkIns.deleteAll()
            goals.deleteAll()
            milestones.deleteAll()
            domains.deleteAll()
            apps.deleteAll()
            events.deleteAll()
            db.settingsDao().deleteAll()
        }
        prefs.clear()
    }

    companion object {
        fun todayStartMillis(): Long =
            LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        fun toLocalDate(millis: Long): LocalDate =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
