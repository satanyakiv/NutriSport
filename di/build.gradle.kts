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
      implementation(project(path = ":data"))
    }
  }
}

android {
  namespace = "com.nutrisport.data"
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
