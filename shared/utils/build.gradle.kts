plugins {
    id("nutrisport.kmp.library")
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.nutrisport.shared.utils"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
        }
    }
}
