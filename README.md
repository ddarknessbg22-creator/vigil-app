# VIGIL — Discipline is a decision.

A native Android application that helps users stop compulsive pornography consumption and reduce exposure to adult content.

**Package:** `com.vigil.app`  
**Min SDK:** 26 | **Target SDK:** 35  
**Architecture:** Clean architecture with UI, data, domain, blocking, and root layers  
**Language:** 100% Kotlin | **UI:** Jetpack Compose + Material 3  
**Build:** Gradle Kotlin DSL (AGP 8.6 / Gradle 8.7)

---

## Deliverables

| Item | Status | Path |
|------|--------|------|
| Complete Android source project | ✅ Implemented | `VigilApp/` |
| Gradle wrapper & build configuration | ✅ Configured | `VigilApp/gradlew`, `build.gradle.kts` |
| GitHub Actions cloud build workflow | ✅ Created | `.github/workflows/build.yml` |
| Debug APK (local build) | ⚠️ See build instructions | `app/build/outputs/apk/debug/app-debug.apk` |
| Release AAB (cloud build) | ☁️ GitHub Actions | `.github/workflows/build.yml` |
| README | ✅ This file | `README.md` |
| Build & install instructions | ✅ Below | See sections |

---

## Feature Status

| Feature | Implemented | Compiled | Tested | Device-Verified |
|---------|------------|----------|--------|-----------------|
| Jetpack Compose + Material 3 UI | ✅ | ⚠️ (AAPT2 ARM64) | ❌ | ❌ |
| Navigation Compose (4 screens) | ✅ | ⚠️ | ❌ | ❌ |
| Liquid Glass theming | ✅ | ⚠️ | ❌ | ❌ |
| Home screen (protection status, goals, stats) | ✅ | ⚠️ | ❌ | ❌ |
| Protection screen (DNS, root, blocklists, apps, tests, recovery) | ✅ | ⚠️ | ❌ | ❌ |
| Progress screen (calendar, milestones, stats, goals, check-in) | ✅ | ⚠️ | ❌ | ❌ |
| Settings screen (protection, notifications, theme, privacy, data) | ✅ | ⚠️ | ❌ | ❌ |
| Room database (check-ins, goals, milestones, domains, apps, events, settings) | ✅ | ⚠️ | ❌ | ❌ |
| DataStore preferences | ✅ | ⚠️ | ❌ | ❌ |
| DNS filtering VPN service | ✅ | ⚠️ | ❌ | ❌ |
| Blocklist management (OISD, Hagezi, custom) | ✅ | ⚠️ | ❌ | ❌ |
| Root-based hosts blocking | ✅ | ⚠️ | ❌ | ❌ |
| WorkManager (blocklist updates, protection monitor) | ✅ | ⚠️ | ❌ | ❌ |
| Broadcast receiver (boot, app update) | ✅ | ⚠️ | ❌ | ❌ |
| Domain matching engine (unit-testable) | ✅ | ✅ | ❌ | ❌ |
| Habit tracking (streaks, milestones, statistics) | ✅ | ⚠️ | ❌ | ❌ |

**Key:** ✅ = implemented in source | ⚠️ = cannot compile locally due to AAPT2 ARM64 limitation | ❌ = not yet tested | ☁️ = GitHub Actions cloud build

---

## Build Instructions (Android Phone)

### Prerequisites

1. **Install Android build tools** once (one approval card):
   - Run `android_sdk_install` with `gradle=true` and `platform=35`
   - This installs OpenJDK 21, Gradle, aapt2, D8, apksigner, and `android.jar` for API 35

2. **Accept Android SDK licenses** (required by Google):
   ```sh
   mkdir -p "$HOME/android-sdk/licenses"
   echo '24333f8a63b6825ea9c5514f83c2829b004d1fee' > "$HOME/android-sdk/licenses/android-sdk-license"
   ```

3. **Set up the project**:
   ```sh
   cd ~/workspace/VigilApp   # or wherever your workspace is
   export ANDROID_HOME="$HOME/android-sdk"
   printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties
   export TMPDIR="$PWD/.tmp"
   mkdir -p "$TMPDIR"
   export GRADLE_USER_HOME="$PWD/.gradle"
   ```

### Building the Debug APK

```sh
export ANDROID_HOME="$HOME/android-sdk"
export TMPDIR="$PWD/.tmp"
mkdir -p "$TMPDIR"
export GRADLE_USER_HOME="$PWD/.gradle"

printf 'sdk.dir=%s\n' "$ANDROID_HOME" > local.properties

bash ./gradlew :app:assembleDebug \
  --no-daemon \
  -Pandroid.aapt2FromMavenOverride="$(command -v aapt2)" \
  -Dorg.gradle.jvmargs="-Xmx1536m -Dorg.gradle.native=false -Djava.io.tmpdir=$TMPDIR" \
  -Dorg.gradle.workers.max=2 \
  -Dorg.gradle.vfs.watch=false \
  -Dkotlin.compiler.execution.strategy=in-process \
  -Dkotlin.daemon.jvm.options="-Xmx1g" \
  -Djava.io.tmpdir="$TMPDIR"
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

### Known Local Build Limitation

**AAPT2 on ARM64 phones:** The Android Gradle Plugin (AGP 8.6) requires `aapt2` version 8.6.0 to process resources. Google only distributes `aapt2` as an x86-64 binary in its Maven repository. There is **no official ARM64 `aapt2` 8.6.0 binary** available.

The system `aapt2` (version 2.20 from the Android 16 SDK) is an ARM64 shared object, but AGP rejects it due to version mismatch.

**Workarounds:**
1. **GitHub Actions cloud build** (recommended): Push to GitHub, the workflow builds a signed debug APK and release AAB on x86-64 runners. Download the artifact from the Actions page.
2. **build-apk.sh** (Gradle-less): For simple Java-only projects, use `scripts/build-apk.sh` from the android-build skill. This does NOT support Kotlin/Compose.

### Installing on Your Device

1. Transfer the APK to your phone (USB, adb, or download from GitHub Actions artifact)
2. Enable "Install from unknown sources" in Settings
3. Open the APK to install
4. Grant the following permissions when prompted:
   - **VPN connection** (required for DNS filtering)
   - **Notifications** (for protection alerts and check-in reminders)
   - **Post notifications** (Android 13+)
   - **Root access** (via Magisk/APatch/Shizuku — optional, for enhanced hosts-based blocking)

### Permissions Used

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Download blocklist updates |
| `ACCESS_NETWORK_STATE` | Check network connectivity |
| `FOREGROUND_SERVICE` | Run DNS filter as foreground service |
| `FOREGROUND_SERVICE_DATA_SYNC` | Background data sync for blocklists |
| `RECEIVE_BOOT_COMPLETED` | Restart protection on device boot |
| `POST_NOTIFICATIONS` | Protection alerts and reminders |
| `WAKE_LOCK` | Keep VPN service alive |
| `BIND_VPN_SERVICE` | DNS filtering VPN tunnel |
| `QUERY_ALL_PACKAGES` | App restriction (block distracting apps) |

---

## Privacy

VIGIL is designed for privacy:

- **No mandatory accounts** — use completely offline
- **No advertisements** — none, ever
- **No analytics by default** — zero telemetry
- **No browsing-history collection** — VIGIL is not a browser and does not inspect your browsing
- **No screenshots or private-message collection**
- **No hidden data transmission** — all network activity is blocklist updates from public GitHub raw URLs
- **All personal data stored locally** — Room database and DataStore on device only
- **No cloud backup of personal data** — `backup_rules.xml` excludes databases and shared preferences

---

## Known Limitations (Important)

VIGIL is a self-discipline aid, not a foolproof content filter. It **cannot** block all explicit content:

1. **Encrypted traffic** — DNS-over-HTTPS (DoH), DNS-over-TLS (DoT), or VPN traffic inside other apps bypasses local DNS filtering
2. **Alternative domains** — New adult domains appear daily; blocklists may not cover them immediately
3. **Legitimate platforms** — Sites like Reddit, Twitter/X, Imgur, etc. host mixed content; VIGIL cannot distinguish safe from adult content on these platforms
4. **In-app content** — Content within apps (not accessed via web browser) is not visible to DNS filtering
5. **Root bypasses** — A rooted device can disable or bypass any app-level restriction
6. **Device owner control** — The device owner can always disable protection or uninstall VIGIL

**Emergency recovery:** VIGIL always provides an "Emergency Disable" option to immediately stop all blocking. The device owner is always in control.

---

## GitHub Actions Cloud Build

Push your code to a GitHub repository and enable Actions. The workflow:

1. Builds a **signed debug APK** on every push/PR
2. Builds a **release AAB** on pushes to `main`
3. Uploads both as downloadable artifacts

Download the APK from the Actions page → Artifacts → `vigil-debug-apk`.

---

## Project Structure

```
VigilApp/
├── build.gradle.kts              # Root build config (AGP, Kotlin plugins)
├── settings.gradle.kts            # Project settings
├── gradle/wrapper/                # Gradle wrapper
├── app/
│   ├── build.gradle.kts           # App build config (Compose, deps)
│   ├── proguard-rules.pro
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/vigil/app/
│   │   │   │   ├── VigilApplication.kt        # App entry, manual DI
│   │   │   │   ├── AppServices.kt             # Service locator
│   │   │   │   ├── data/                      # Room, DataStore, Repository
│   │   │   │   │   ├── VigilRepository.kt
│   │   │   │   │   ├── datastore/PreferencesDataStore.kt
│   │   │   │   │   └── room/                  # DAOs, entities, database
│   │   │   │   ├── domain/DomainMatcher.kt    # Pure-Kotlin domain matching
│   │   │   │   ├── blocking/                  # Blocklist manager, root
│   │   │   │   │   ├── BlocklistManager.kt
│   │   │   │   │   ├── root/RootManager.kt
│   │   │   │   │   ├── vpn/VigilVpnService.kt # DNS filter VPN service
│   │   │   │   │   ├── vpn/DnsPacket.kt       # DNS wire-format parser
│   │   │   │   │   └── worker/                # WorkManager workers
│   │   │   │   └── ui/                        # Compose UI (theme, components, screens)
│   │   │   │       ├── theme/Color.kt, Type.kt, Shape.kt
│   │   │   │       ├── components/GlassSurface.kt
│   │   │   │       ├── home/HomeScreen.kt
│   │   │   │       ├── protection/ProtectionScreen.kt
│   │   │   │       ├── progress/ProgressScreen.kt
│   │   │   │       ├── settings/SettingsScreen.kt
│   │   │   │       ├── navigation/NavHost.kt
│   │   │   │       └── main/MainActivity.kt
│   │   │   └── res/
│   │   │       ├── values/strings.xml
│   │   │       └── xml/ (backup rules)
│   │   └── test/                            # Unit tests
│   └── schemas/                            # Room export schemas
├── .github/workflows/build.yml            # Cloud build CI
├── .gitignore
└── README.md
```

---

## Remaining Tasks

1. **Fix local AAPT2 ARM64 build** — Obtain or build an ARM64 `aapt2` 8.6.0 binary and patch it into the Gradle cache
2. **Run unit tests** — Add JUnit tests for `DomainMatcher` and `VigilRepository`
3. **Device testing** — Install the debug APK on a rooted Android device and verify:
   - DNS filtering blocks known adult domains
   - VPN service starts and persists
   - Root hosts blocking works (if rooted)
   - All screens navigate correctly
   - Data persists across app restarts
4. **Add app icons** — Create adaptive icon mipmap resources
5. **Add splash screen** — Android 12+ splash screen compatible
6. **Polish UI animations** — Add fluid transitions between screens
7. **Add export/import** — Implement data export to JSON and import from backup
