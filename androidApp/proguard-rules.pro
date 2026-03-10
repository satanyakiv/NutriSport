# ---- App models (keep Serializable classes for navigation & JSON) ----
-keep @kotlinx.serialization.Serializable class com.nutrisport.** { *; }
-keep @kotlinx.serialization.Serializable class com.portfolio.** { *; }

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ---- Firebase (KMP via dev.gitlive) ----
-keep class dev.gitlive.firebase.** { *; }
-dontwarn dev.gitlive.firebase.**

# ---- KMPAuth ----
-keep class com.mmk.kmpauth.** { *; }
-dontwarn com.mmk.kmpauth.**

# ---- Kotlin Serialization ----
-keepattributes *Annotation*, InnerClasses, Signature
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.nutrisport.**$$serializer { *; }
-keep,includedescriptorclasses class com.portfolio.**$$serializer { *; }
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
-keep class io.ktor.client.engine.** { *; }
-keep class io.ktor.serialization.** { *; }

# ---- Koin ----
-keep class org.koin.core.** { *; }
-keep class org.koin.mp.** { *; }
-dontwarn org.koin.**

# ---- Coil ----
-dontwarn coil3.**

# ---- Napier ----
-dontwarn io.github.aakira.napier.**
