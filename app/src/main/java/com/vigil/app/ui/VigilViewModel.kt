package com.vigil.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vigil.app.AppServices
import com.vigil.app.blocking.BlocklistManager
import com.vigil.app.blocking.root.RootManager
import com.vigil.app.blocking.vpn.VigilVpnService
import com.vigil.app.data.VigilRepository
import com.vigil.app.data.datastore.PrefKeys
import com.vigil.app.data.room.BlockedDomainEntity
import com.vigil.app.data.room.CheckInEntity
import com.vigil.app.data.room.GoalEntity
import com.vigil.app.domain.DomainMatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Everything the screens need, in one lifecycle-aware place. */
class VigilViewModel(app: Application) : AndroidViewModel(app) {

    private val repo: VigilRepository = AppServices.app(app).repository
    private val prefs = repo.prefs
    private val blocklist: BlocklistManager = AppServices.blocklist(app)
    private val root: RootManager = AppServices.root(app)

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Stats that need suspend calls, refreshed on demand. */
    data class Stats(
        val streak: Int = 0,
        val best: Int = 0,
        val checkIns: Int = 0,
        val relapses: Int = 0,
        val blocked: Int = 0
    )

    private val _stats = MutableStateFlow(Stats())
    val stats: StateFlow<Stats> = _stats.asStateFlow()

    val protectionEnabled: StateFlow<Boolean> =
        prefs.bool(PrefKeys.PROTECTION_ENABLED, true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val dnsEnabled: StateFlow<Boolean> =
        prefs.bool(PrefKeys.DNS_ENABLED, true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val rootEnabled: StateFlow<Boolean> =
        prefs.bool(PrefKeys.ROOT_ENABLED, false)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val autoUpdate: StateFlow<Boolean> =
        prefs.bool(PrefKeys.AUTO_UPDATE, true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val logBlocked: StateFlow<Boolean> =
        prefs.bool(PrefKeys.LOG_BLOCKED, false)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val appRestrictions: StateFlow<Boolean> =
        prefs.bool(PrefKeys.APP_RESTRICTIONS, false)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val friction: StateFlow<Boolean> =
        prefs.bool(PrefKeys.FRICTION, true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val dailyReminders: StateFlow<Boolean> =
        prefs.bool(PrefKeys.NOTIFY_DAILY, true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val glassEnabled: StateFlow<Boolean> =
        prefs.bool(PrefKeys.LIQUID_GLASS, true)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val themeMode: StateFlow<String> =
        prefs.string(PrefKeys.THEME_MODE, "system")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "system")

    val domains: StateFlow<List<BlockedDomainEntity>> =
        repo.domainsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val goals: StateFlow<List<GoalEntity>> =
        repo.activeGoals()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val checkIns: StateFlow<List<CheckInEntity>> =
        repo.checkInFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _today = MutableStateFlow<CheckInEntity?>(null)
    val today: StateFlow<CheckInEntity?> = _today.asStateFlow()

    private val _rootStatus = MutableStateFlow(root.status())
    val rootStatus: StateFlow<RootManager.Status> = _rootStatus.asStateFlow()

    val vpnRunning: StateFlow<Boolean> = VigilVpnService.isRunning

    init {
        viewModelScope.launch {
            blocklist.init()
            _rootStatus.value = root.status()
            refresh()
        }
    }

    suspend fun refresh() {
        val totals = repo.totals()
        _today.value = repo.todayCheckIn()
        _stats.value = Stats(
            streak = repo.streakDays(),
            best = repo.longestStreak(),
            checkIns = totals.checkIns,
            relapses = totals.relapses,
            blocked = totals.blocked
        )
    }

    fun retryRoot() {
        viewModelScope.launch {
            _rootStatus.value = root.requestAccess()
            _message.value = when (_rootStatus.value) {
                RootManager.Status.AVAILABLE -> "Root granted. Hosts blocking is available."
                RootManager.Status.DENIED -> "Root request was denied."
                RootManager.Status.NOT_ROOTED -> "No root manager found on this device."
                RootManager.Status.UNKNOWN -> "Root state is still unknown."
            }
        }
    }

    fun setProtection(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBool(PrefKeys.PROTECTION_ENABLED, enabled)
            if (enabled) {
                VigilVpnService.start(getApplication())
                repo.logEvent("protection_enabled", "Enabled from UI")
            } else {
                VigilVpnService.stop(getApplication())
                if (rootEnabled.value) {
                    root.clearHosts()
                }
                repo.logEvent("protection_disabled", "Disabled from UI")
            }
            refresh()
        }
    }

    fun setFlag(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch { prefs.setBool(key, value) }
    }

    fun setTheme(mode: String) {
        viewModelScope.launch { prefs.setString(PrefKeys.THEME_MODE, mode) }
    }

    fun addDomain(raw: String, allowed: Boolean) {
        viewModelScope.launch {
            val result = repo.addDomain(raw, if (allowed) "allowed" else "blocked")
            result.fold(
                onSuccess = {
                    blocklist.reload()
                    _message.value = "Added $it"
                    refresh()
                },
                onFailure = { _message.value = it.message ?: "Could not add that domain" }
            )
        }
    }

    fun removeDomain(domain: String) {
        viewModelScope.launch {
            repo.removeDomain(domain)
            blocklist.reload()
            _message.value = "Removed $domain"
            refresh()
        }
    }

    fun updateBlocklist() {
        viewModelScope.launch {
            _busy.value = true
            _message.value = "Downloading blocklist…"
            runCatching {
                blocklist.updateFromRemote(
                    BlocklistManager.SOURCE_OISD,
                    BlocklistManager.OISD_FULL
                )
            }.fold(
                onSuccess = {
                    prefs.setLong(PrefKeys.LAST_BLOCKLIST_UPDATE, System.currentTimeMillis())
                    repo.logEvent("blocklist_updated", "$it domains")
                    _message.value = "Blocklist updated: $it domains"
                    refresh()
                },
                onFailure = {
                    _message.value = "Update failed: ${it.message ?: "network error"}"
                    repo.logEvent("blocklist_update_failed", it.message ?: "unknown")
                }
            )
            _busy.value = false
        }
    }

    /** Runs a real name through the same matcher the DNS filter uses. */
    fun testBlocking(host: String) {
        viewModelScope.launch {
            blocklist.reload()
            val normalized = DomainMatcher.normalize(host)
            val valid = DomainMatcher.isValidDomain(normalized)
            val blocked = blocklist.isBlocked(normalized)
            val result = when {
                !valid -> "“$host” is not a valid domain name."
                blocked -> "BLOCKED — $normalized matched the blocklist."
                else -> "ALLOWED — $normalized is not on any blocklist."
            }
            _message.value = result
            prefs.setString(PrefKeys.LAST_TEST_RESULT, result)
            repo.logEvent("test_run", result)
        }
    }

    fun saveCheckIn(mood: Int, notes: String, urge: Boolean, relapse: Boolean) {
        viewModelScope.launch {
            repo.saveCheckIn(mood, notes, urge, relapse)
            refresh()
            _message.value = if (relapse) {
                "Check-in saved. A setback is information, not a verdict."
            } else {
                "Check-in saved."
            }
        }
    }

    fun createGoal(title: String, description: String, days: Int) {
        viewModelScope.launch {
            repo.createGoal(title, description, days)
            _message.value = "Goal created."
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repo.deleteGoal(goal)
            _message.value = "Goal deleted."
        }
    }

    fun setAppRestriction(pkg: String, name: String, restricted: Boolean) {
        viewModelScope.launch { repo.setAppRestriction(pkg, name, restricted) }
    }

    fun applyRootHosts() {
        viewModelScope.launch {
            _busy.value = true
            val domains = repo.blockedSet()
            root.applyHosts(domains).fold(
                onSuccess = {
                    prefs.setBool(PrefKeys.ROOT_ENABLED, true)
                    _message.value = "Hosts file updated with $it domains."
                },
                onFailure = { _message.value = it.message ?: "Root write failed" }
            )
            _busy.value = false
        }
    }

    fun clearRootHosts() {
        viewModelScope.launch {
            root.clearHosts()
            prefs.setBool(PrefKeys.ROOT_ENABLED, false)
            _message.value = "Hosts entries removed."
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            prefs.setBool(PrefKeys.PROTECTION_ENABLED, false)
            VigilVpnService.stop(getApplication())
            runCatching { root.clearHosts() }
            repo.logEvent("emergency_disable", "Protection stopped from UI")
            _message.value = "Protection stopped. Nothing is blocking right now."
            refresh()
        }
    }

    fun wipeData() {
        viewModelScope.launch {
            repo.wipeEverything()
            blocklist.reload()
            _message.value = "All local data deleted."
            refresh()
        }
    }

    fun clearMessage() { _message.value = null }

    fun exportSummary(): String {
        val s = _stats.value
        return buildString {
            appendLine("VIGIL export")
            appendLine("generated: ${System.currentTimeMillis()}")
            appendLine("check_ins: ${s.checkIns}")
            appendLine("relapses: ${s.relapses}")
            appendLine("current_streak: ${s.streak}")
            appendLine("longest_streak: ${s.best}")
            appendLine("blocked_events: ${s.blocked}")
            appendLine("domains: ${domains.value.size}")
            appendLine("goals: ${goals.value.size}")
        }
    }
}
