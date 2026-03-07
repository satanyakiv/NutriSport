plugins {
    id("nutrisport.kmp.library")
    alias(libs.plugins.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.nutrisport.navigation"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
            implementation(libs.compose.navigation)

            implementation(project(":feature:auth"))
            implementation(project(":feature:home"))
            implementation(project(":feature:home:categories:search"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:adminPanel"))
            implementation(project(":feature:details"))
            implementation(project(":feature:home:cart"))
            implementation(project(":feature:home:cart:checkout"))
            implementation(project(":feature:home:cart:checkout:paymentCompleted"))
            implementation(project(":feature:adminPanel:manageProduct"))
            implementation(project(":shared"))
        }
    }
}
