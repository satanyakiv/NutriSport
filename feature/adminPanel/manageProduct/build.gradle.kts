plugins {
    id("nutrisport.kmp.feature.full")
}

kotlin {
    androidLibrary {
        namespace = "com.nutrisport.manage_product"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        commonMain.dependencies {
            implementation(libs.firebase.storage)
            implementation(project(":shared"))
            implementation(project(":data"))
        }
    }
}
