plugins {
    id("nutrisport.kmp.library")
}

kotlin {
    android {
        namespace = "com.nutrisport.analytics.firebase"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        commonMain.dependencies {
            implementation(libs.firebase.analytics)
            implementation(libs.koin.core)
            implementation(project(":analytics:core"))
        }
    }
}
