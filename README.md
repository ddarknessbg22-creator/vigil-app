# VIGIL

A private, offline Android journal for personal commitments. A streak measures elapsed time from a chosen instant; missing a check-in does not end it. Starting again archives the previous run without erasing reflections or personal records.

Home, Quotes, Progress and Settings use Kotlin, Compose Material 3 and Navigation Compose. Room holds journal entries, goals, streak intervals and reading history. DataStore holds appearance and reminder preferences. No account or internet permission is required. Reminders are opt-in, approximate local-time notifications. Exported JSON backups are not encrypted: choose a private location.

Android 8+ (API 26). Build the debug APK with JDK 17, Android SDK 35 and `./gradlew :app:testDebugUnitTest :app:assembleDebug`. The Build VIGIL APK workflow publishes a debug artifact after a successful build. An upgrade requires the same Android signing key as the installed app.
