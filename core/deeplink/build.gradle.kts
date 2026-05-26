plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.core.deeplink"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(project(":domain"))
            implementation(project(":shared:utils"))
        }
        commonTest.dependencies {
            implementation(project(":shared:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.assertk)
        }
    }
}
