plugins {
    id("nutrisport.kmp.library")
}

kotlin {
    android {
        namespace = "com.nutrisport.analytics.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(project(":domain"))
        }
    }
}
