plugins {
    id("nutrisport.kmp.feature")
}

kotlin {
    android {
        namespace = "com.nutrisport.home"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.navigation)
            implementation(project(":shared"))
            implementation(project(":data"))
            implementation(project(":feature:home:productsOverview"))
            implementation(project(":feature:home:categories"))
            implementation(project(":feature:home:cart"))
        }
    }
}
