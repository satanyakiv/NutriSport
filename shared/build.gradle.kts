plugins {
    id("nutrisport.kmp.library")
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.nutrisport.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.android.client)
        }
        iosMain.dependencies {
            implementation(libs.ktor.darwin.client)
        }
        commonMain.dependencies {
            implementation(libs.bundles.coil)
            implementation(libs.kotlinx.serialization)
        }
    }
}
