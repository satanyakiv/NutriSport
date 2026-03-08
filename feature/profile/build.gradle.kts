plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.profile"
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
