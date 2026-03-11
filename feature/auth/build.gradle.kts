plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.auth"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
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
            implementation(project(":shared:utils"))
            implementation(project(":shared:ui"))
            implementation(project(":domain"))
        }
        commonTest.dependencies {
            implementation(project(":shared:testing"))
        }
    }
}
