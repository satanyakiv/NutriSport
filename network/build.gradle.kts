plugins {
    id("nutrisport.kmp.library")
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.nutrisport.network"
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
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.auth.firebase.kmp)
            implementation(libs.kotlinx.serialization)
            implementation(project(":domain"))
            implementation(project(":database"))
        }
    }
}
