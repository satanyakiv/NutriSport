plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.admin_panel"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        commonMain.dependencies {
            implementation(libs.firebase.storage)
            implementation(project(":shared:utils"))
            implementation(project(":shared:ui"))
            implementation(project(":data"))
        }
        commonTest.dependencies {
            implementation(project(":shared:test-fixtures"))
        }
    }
}
