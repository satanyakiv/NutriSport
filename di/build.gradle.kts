import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  compilerOptions {
    freeCompilerArgs.add("-Xreturn-value-checker=full")
  }

  listOf(
    iosArm64(),
    iosX64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "di"
      isStatic = true
    }
  }

  sourceSets {
    commonMain.dependencies {
      implementation(compose.runtime)
      implementation(compose.foundation)
      implementation(compose.material3)
      implementation(compose.ui)
      implementation(compose.components.resources)
      implementation(compose.components.uiToolingPreview)

      implementation(libs.koin.core)
      implementation(libs.koin.compose.viewmodel)
      implementation(libs.koin.compose)

      implementation(project(path = ":feature:auth"))
      implementation(project(path = ":feature:home"))
      implementation(project(path = ":feature:home:productsOverview"))
      implementation(project(path = ":feature:home:categories"))
      implementation(project(path = ":feature:home:categories:search"))
      implementation(project(path = ":feature:details"))
      implementation(project(":feature:home:cart"))
      implementation(project(":feature:home:cart:checkout"))
      implementation(project(":feature:home:cart:checkout:paymentCompleted"))
      implementation(project(path = ":feature:profile"))
      implementation(project(path = ":feature:adminPanel"))
      implementation(project(path = ":feature:adminPanel:manageProduct"))
      implementation(project(path = ":data"))
    }
  }
}

android {
  namespace = "com.nutrisport.di"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}
