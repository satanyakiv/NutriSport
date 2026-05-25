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

# ---- Google Firebase ComponentRegistrar ----
# Firebase Crashlytics / Common / Installations register components via the SPI
# file META-INF/com.google.firebase.components.ComponentRegistrar. R8 full-mode
# (AGP 8+ default) strips the no-arg constructors of those Registrars because
# nothing references them directly. They are instantiated reflectively at
# FirebaseInitProvider start-up. Without this keep, Crashlytics never initialises
# in release and uncaught crashes are silently dropped before reaching Console.
# Firebase BoM does NOT ship this rule in its consumer ProGuard files.
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    <init>();
}

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
