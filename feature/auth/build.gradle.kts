plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    androidLibrary {
        namespace = "com.nutrisport.auth"
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
            implementation(libs.auth.kmp)
            implementation(libs.auth.firebase.kmp)
            implementation(project(":shared"))
            implementation(project(":data"))
        }
    }
}
