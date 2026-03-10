rootProject.name = "NutriSport"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
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
include(":feature:home:cart")
include(":feature:home:cart:checkout")
include(":feature:home:categories")
include(":feature:details")
include(":feature:home")
include(":feature:home:categories:search")
include(":feature:home:productsOverview")
include(":feature:profile")
include(":feature:auth")
include(":navigation")
include(":composeApp")
include(":androidApp")
include(":feature:home:cart:checkout:paymentCompleted")
include(":shared:utils")
include(":shared:ui")
include(":shared:test-fixtures")
include(":analytics")
include(":database")
