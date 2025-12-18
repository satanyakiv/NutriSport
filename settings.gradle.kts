rootProject.name = "NutriSport"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}
include(":data")
include(":di")

include(":feature:home")
include(":feature:auth")
include(":navigation")
include(":composeApp")
include(":shared")
