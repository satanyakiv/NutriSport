plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly("com.android.tools.build:gradle:${libs.versions.agp.get()}")
    compileOnly("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:${libs.versions.kotlin.get()}")
    compileOnly("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:${libs.versions.compose.multiplatform.get()}")
    compileOnly("org.jetbrains.kotlin:compose-compiler-gradle-plugin:${libs.versions.kotlin.get()}")
    compileOnly("dev.mokkery:mokkery-gradle:${libs.versions.mokkery.get()}")
}

gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "nutrisport.kmp.library"
            implementationClass = "NutriSportKmpLibraryPlugin"
        }
        register("kmpFeature") {
            id = "nutrisport.kmp.feature"
            implementationClass = "NutriSportKmpFeaturePlugin"
        }
        register("kmpFeatureFull") {
            id = "nutrisport.kmp.feature.full"
            implementationClass = "NutriSportKmpFeatureFullPlugin"
        }
    }
}
