plugins {
  id("nutrisport.kmp.library")
  alias(libs.plugins.serialization)
}

kotlin {
  android {
    namespace = "com.nutrisport.domain"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    withHostTest {}
  }
  sourceSets {
    commonMain.dependencies {
      implementation(libs.kotlinx.serialization)
    }
    commonTest.dependencies {
      implementation(project(":shared:testing"))
    }
  }
}
