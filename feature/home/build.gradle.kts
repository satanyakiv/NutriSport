plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.home"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.navigation)
            implementation(project(":shared:utils"))
            implementation(project(":shared:ui"))
            implementation(project(":domain"))
            implementation(project(":feature:home:productsOverview"))
            implementation(project(":feature:home:categories"))
            implementation(project(":feature:home:cart"))
        }
        commonTest.dependencies {
            implementation(project(":shared:testing"))
        }
    }
}
