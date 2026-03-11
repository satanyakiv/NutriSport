import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.mokkery) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.detekt) apply false
}

dependencies {
    kover(project(":domain"))
    kover(project(":shared:utils"))
    kover(project(":shared:ui"))
    kover(project(":network"))
    kover(project(":database"))
    kover(project(":di"))
    kover(project(":navigation"))
    kover(project(":analytics"))
    kover(project(":feature:auth"))
    kover(project(":feature:home"))
    kover(project(":feature:home:productsOverview"))
    kover(project(":feature:home:categories"))
    kover(project(":feature:home:categories:search"))
    kover(project(":feature:details"))
    kover(project(":feature:home:cart"))
    kover(project(":feature:home:cart:checkout"))
    kover(project(":feature:home:cart:checkout:paymentCompleted"))
    kover(project(":feature:profile"))
    kover(project(":feature:adminPanel"))
    kover(project(":feature:adminPanel:manageProduct"))
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*Screen*", "*Preview*", "*ComposableSingletons*",
                    "com.nutrisport.database.dao.*", "com.nutrisport.database.entity.*",
                    "com.nutrisport.database.NutriSportDatabase*", "com.nutrisport.database.converter.*",
                    "com.nutrisport.di.*", "com.nutrisport.navigation.*",
                    "com.portfolio.nutrisport.MainActivity*", "com.portfolio.nutrisport.NutrisportApplication*",
                    "com.nutrisport.shared.Resources*", "com.nutrisport.shared.component.*",
                    "com.nutrisport.shared.Alpha*", "com.nutrisport.shared.Colors*",
                    "com.nutrisport.shared.Fonts*", "com.nutrisport.shared.Constants*",
                    "*BuildConfig*",
                )
            }
        }
    }
}

val detektVersion = libs.versions.detekt.get()

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<DetektExtension> {
        config.setFrom("${rootProject.projectDir}/detekt/config.yml")
        autoCorrect = true
        parallel = true
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
            "src/main/kotlin",
            "src/main/java",
        )
        buildUponDefaultConfig = false
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        exclude { element ->
            element.file.path.contains("/build/generated/")
        }
    }

    dependencies {
        "detektPlugins"("io.gitlab.arturbosch.detekt:detekt-formatting:$detektVersion")
    }
}
