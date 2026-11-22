# VIGIL ProGuard Rules

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class com.vigil.app.Hilt_* { *; }

# Keep Room entities and DAOs
-keep class com.vigil.app.data.room.** { *; }

# Keep DataStore preferences
-keep class com.vigil.app.data.datastore.** { *; }

# Keep WorkManager workers
-keep class com.vigil.app.blocking.worker.** { *; }

# Keep VPN service
-keep class com.vigil.app.blocking.vpn.** { *; }

# Keep serialization classes
-keep class kotlinx.serialization.** { *; }

# Keep Coil
-keep class coil3.** { *; }

# Keep Accompanist
-keep class com.google.accompanist.** { *; }

# Keep Material3
-keep class androidx.compose.material3.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }

# Keep Lifecycle
-keep class androidx.lifecycle.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep RxJava
-keep class io.reactivex.rxjava3.** { *; }

# Suppress warnings
-dontwarn kotlinx.coroutines.**
-dontwarn io.reactivex.rxjava3.**
-dontwarn androidx.datastore.**
-dontwarn dagger.hilt.**
-dontwarn coil3.**
