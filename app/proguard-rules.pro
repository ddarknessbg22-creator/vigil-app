# VIGIL R8 rules — minimal, correctness first.

# Room: keep entities (reflectively constructed) and generated implementations.
-keep class com.vigil.app.data.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Kotlinx serialization generated serializers
-keepattributes *Annotation*, InnerClasses, Signature
-keep class kotlinx.serialization.json.** { *; }
-keep class com.vigil.app.data.**$$serializer { *; }
-keepclassmembers class com.vigil.app.data.** {
    *** companion;
}
-keepclasseswithmembers class com.vigil.app.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Compose / Android runtime reflectively instantiated classes
-keep class com.vigil.app.ui.main.MainActivity { *; }
-keep class com.vigil.app.StreakWidgetProvider { *; }
-keep class com.vigil.app.ReminderReceiver { *; }
-keep class com.vigil.app.VigilApplication { *; }

# DataStore
-keep class androidx.datastore.** { *; }

# Coroutines debug suppressions
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.datastore.**
