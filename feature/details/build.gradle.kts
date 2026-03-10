plugins {
    id("nutrisport.kmp.feature.full")
}

kotlin {
    android {
        namespace = "com.nutrisport.details"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:utils"))
            implementation(project(":shared:ui"))
            implementation(project(":data"))
        }
        commonTest.dependencies {
            implementation(project(":shared:test-fixtures"))
        }
    }
}
