# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ---- Kotlin Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nutrisport.**$$serializer { *; }
-keepclassmembers class com.nutrisport.** {
    *** Companion;
}
-keepclasseswithmembers class com.nutrisport.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Compose ----
-dontwarn androidx.compose.**

# ---- Ktor ----
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

# ---- Koin ----
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ---- General ----
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn kotlin.**
-dontwarn kotlinx.**
