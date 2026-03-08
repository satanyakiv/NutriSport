plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.shared.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    // CMP-9547: enable androidResources for Compose Resources with AGP 9.x
    androidLibrary {
        androidResources.enable = true
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.android.client)
        }
        iosMain.dependencies {
            implementation(libs.ktor.darwin.client)
        }
        commonMain.dependencies {
            implementation(project(":shared:utils"))
            implementation(libs.bundles.coil)
            implementation(libs.kotlinx.serialization)
        }
    }
}
