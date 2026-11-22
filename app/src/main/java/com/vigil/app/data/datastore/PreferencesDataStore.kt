package com.vigil.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/** Preference keys used across the app. */
object PrefKeys {
    val PROTECTION_ENABLED = booleanPreferencesKey("protection_enabled")
    val DNS_ENABLED = booleanPreferencesKey("dns_enabled")
    val ROOT_ENABLED = booleanPreferencesKey("root_enabled")
    val AUTO_UPDATE = booleanPreferencesKey("auto_update")
    val LOG_BLOCKED = booleanPreferencesKey("log_blocked")
    val APP_RESTRICTIONS = booleanPreferencesKey("app_restrictions")
    val FRICTION = booleanPreferencesKey("friction_mode")
    val BOOT_PERSISTENCE = booleanPreferencesKey("boot_persistence")
    val DOH_BYPASS = booleanPreferencesKey("doh_bypass_block")
    val EMERGENCY_DISABLE = booleanPreferencesKey("emergency_disable")

    val NOTIFY_DAILY = booleanPreferencesKey("notify_daily")
    val NOTIFY_CHECKIN = booleanPreferencesKey("notify_checkin")
    val NOTIFY_PROTECTION = booleanPreferencesKey("notify_protection")
    val NOTIFY_WEEKLY = booleanPreferencesKey("notify_weekly")

    val THEME_MODE = stringPreferencesKey("theme_mode")           // system | dark | light
    val LIQUID_GLASS = booleanPreferencesKey("liquid_glass")
    val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")

    val SELECTED_BLOCKLIST = stringPreferencesKey("selected_blocklist")
    val LAST_BLOCKLIST_UPDATE = longPreferencesKey("last_blocklist_update")
    val LAST_TEST_RESULT = stringPreferencesKey("last_test_result")
    val COOLDOWN_MINUTES = intPreferencesKey("cooldown_minutes")
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vigil_prefs")

/**
 * Typed wrapper over Jetpack DataStore. All reads are cold Flows so the UI
 * updates itself when a setting changes anywhere in the app.
 */
class PreferencesDataStore(private val context: Context) {

    private val prefs: Flow<Preferences> = context.dataStore.data
        .catch { emit(emptyPreferences()) }

    fun bool(key: Preferences.Key<Boolean>, default: Boolean): Flow<Boolean> =
        prefs.map { it[key] ?: default }

    fun string(key: Preferences.Key<String>, default: String = ""): Flow<String> =
        prefs.map { it[key] ?: default }

    fun long(key: Preferences.Key<Long>, default: Long = 0L): Flow<Long> =
        prefs.map { it[key] ?: default }

    fun int(key: Preferences.Key<Int>, default: Int = 0): Flow<Int> =
        prefs.map { it[key] ?: default }

    suspend fun setBool(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun setString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun setLong(key: Preferences.Key<Long>, value: Long) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun setInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { it[key] = value }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
