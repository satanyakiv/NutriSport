plugins {
    id("nutrisport.kmp.library")
}

kotlin {
    android {
        namespace = "com.nutrisport.shared.testing"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":domain"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
