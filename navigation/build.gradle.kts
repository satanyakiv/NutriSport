import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.serialization)
}

kotlin {
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
  }

  listOf(
    iosArm64(),
    iosX64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "navigation"
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
      implementation(libs.kotlinx.serialization)
      implementation(libs.compose.navigation)

      implementation(project(":feature:auth"))
      implementation(project(":feature:home"))
    }
  }
}

android {
  namespace = "com.portfolio.navigation"
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
