plugins {
    id("nutrisport.kmp.library")
}

kotlin {
    android {
        namespace = "com.nutrisport.di"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTest {}
    }
    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=full")
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            implementation(libs.bundles.koin.compose)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            implementation(project(":feature:auth"))
            implementation(project(":feature:home"))
            implementation(project(":feature:home:productsOverview"))
            implementation(project(":feature:home:categories"))
            implementation(project(":feature:home:categories:search"))
            implementation(project(":feature:details"))
            implementation(project(":feature:home:cart"))
            implementation(project(":feature:home:cart:checkout"))
            implementation(project(":feature:home:cart:checkout:paymentCompleted"))
            implementation(project(":feature:profile"))
            implementation(project(":feature:adminPanel"))
            implementation(project(":feature:adminPanel:manageProduct"))
            implementation(project(":analytics:core"))
            implementation(project(":analytics:firebase"))
            implementation(project(":navigation"))
            implementation(project(":domain"))
            implementation(project(":network"))
            implementation(project(":database"))
            implementation(project(":shared:utils"))
            implementation(project(":shared:testing"))
        }
    }
}
