plugins {
    id("nutrisport.kmp.feature.full")
}

kotlin {
    android {
        namespace = "com.nutrisport.products_overview"
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
