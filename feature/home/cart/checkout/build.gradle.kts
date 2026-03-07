plugins {
    id("nutrisport.kmp.feature.full")
}

kotlin {
    androidLibrary {
        namespace = "com.nutrisport.checkout"
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
