plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    androidLibrary {
        namespace = "com.nutrisport.admin_panel"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared"))
            implementation(project(":data"))
        }
    }
}
