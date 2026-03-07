plugins {
    id("nutrisport.kmp.library")
}

kotlin {
    androidLibrary {
        namespace = "com.nutrisport.data"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=full")
    }
    sourceSets {
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
        }
        commonMain.dependencies {
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.auth.firebase.kmp)
            implementation(project(":shared"))
        }
    }
}
