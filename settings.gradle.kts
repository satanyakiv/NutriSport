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
include(":feature:adminPanel")

include(":feature:home")
include(":feature:profile")
include(":feature:auth")
include(":navigation")
include(":composeApp")
include(":shared")
