import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.google.services)
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
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)

        implementation(libs.koin.core)
        implementation(libs.koin.compose)

        implementation(libs.auth.kmp)
        implementation(libs.firebase.app)

        implementation(project(":navigation"))
        implementation(project(":shared"))
        implementation(project(":di"))
        implementation(project(":data"))
      }
    }

    val androidMain by getting {
      dependencies {
        implementation(project.dependencies.platform(libs.firebase.bom))
        implementation(compose.preview)
        implementation(libs.androidx.activity.compose)
        implementation(libs.splash.screen)
        implementation(compose.material3)
        implementation(libs.koin.android)
      }
    }

    // Shared iOS source set for all iOS targets.
    // Put iOS `actual` implementations into: composeApp/src/iosMain/kotlin
    val iosMain by creating {
      dependsOn(commonMain)
    }

    val iosArm64Main by getting {
      dependsOn(iosMain)
    }

    val iosSimulatorArm64Main by getting {
      dependsOn(iosMain)
    }
  }
}

android {
  namespace = "com.portfolio.nutrisport"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.portfolio.nutrisport"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
}

dependencies {
//    implementation(libs.core.splashscreen)
  debugImplementation(compose.uiTooling)
}
