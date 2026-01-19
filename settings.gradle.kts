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
include(":feature:home:cart")
include(":feature:home:cart:checkout")
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
include(":feature:home:cart:checkout:paymentCompleted")
include(":search")
include(":shared")
