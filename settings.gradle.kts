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
include(":feature:adminPanel:manageProduct")
include(":feature:cart")
include(":feature:home:categories")
include(":feature:details")
include(":feature:home")
include(":feature:home:categories:search")
include(":feature:home:category:search")
include(":feature:home:category:search")
include(":feature:home:productsOverview")
include(":feature:profile")
include(":feature:auth")
include(":navigation")
include(":composeApp")
include(":search")
include(":shared")
