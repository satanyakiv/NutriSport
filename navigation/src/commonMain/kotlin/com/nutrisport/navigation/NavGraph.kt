package com.nutrisport.navigation

import CategorySearchScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nutrisport.admin_panel.AdminPanelScreen
import com.nutrisport.auth.component.AuthScreen
import com.nutrisport.checkout.CheckoutScreen
import com.nutrisport.details.DetailsScreen
import com.nutrisport.home.HomeGraphScreen
import com.nutrisport.manage_product.ManageProductScreen
import com.nutrisport.profile.ProfileScreen
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.navigation.Screen
import com.portfolio.payment_completed.PaymentCompletedScreen

@Composable
fun SetupNavGraph(startDestination: Screen = Screen.Auth) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = startDestination,
  ) {
    authDestination(navController)
    homeGraphDestination(navController)
    profileDestination(navController)
    adminDestinations(navController)
    detailsDestination(navController)
    categorySearchDestination(navController)
    checkoutDestinations(navController)
  }
}

private fun NavGraphBuilder.authDestination(navController: NavController) {
  composable<Screen.Auth> {
    AuthScreen(
      goToHome = {
        navController.navigate(Screen.HomeGraph) {
          popUpTo<Screen.Auth> { inclusive = true }
        }
      },
    )
  }
}

private fun NavGraphBuilder.homeGraphDestination(navController: NavController) {
  composable<Screen.HomeGraph> {
    HomeGraphScreen(
      navigateToAuth = {
        navController.navigate(Screen.Auth) {
          popUpTo<Screen.HomeGraph> { inclusive = true }
        }
      },
      navigateToProfile = {
        navController.navigate(Screen.Profile)
      },
      navigateToAdminPanel = {
        navController.navigate(Screen.AdminPanel)
      },
      navigateToDetails = { productId ->
        navController.navigate(Screen.Details(id = productId))
      },
      navigateToCategorySearch = { categoryName ->
        navController.navigate(Screen.CategorySearch(categoryName))
      },
      navigateToCheckout = { totalAmount ->
        navController.navigate(Screen.Checkout(totalAmount))
      },
    )
  }
}

private fun NavGraphBuilder.profileDestination(navController: NavController) {
  composable<Screen.Profile> {
    ProfileScreen(
      goBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.adminDestinations(navController: NavController) {
  composable<Screen.AdminPanel> {
    AdminPanelScreen(
      goBack = { navController.navigateUp() },
      goToManageProduct = { id ->
        navController.navigate(Screen.ManageProduct(id = id))
      },
    )
  }
  composable<Screen.ManageProduct> {
    val id = it.toRoute<Screen.ManageProduct>().id
    ManageProductScreen(
      id = id,
      goBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.detailsDestination(navController: NavController) {
  composable<Screen.Details> {
    DetailsScreen(
      goBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.categorySearchDestination(navController: NavController) {
  composable<Screen.CategorySearch> {
    val category = ProductCategory.valueOf(it.toRoute<Screen.CategorySearch>().category)
    CategorySearchScreen(
      category = category,
      navigateToDetails = { id ->
        navController.navigate(Screen.Details(id))
      },
      navigateBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.checkoutDestinations(navController: NavController) {
  composable<Screen.Checkout> {
    val totalAmount = it.toRoute<Screen.Checkout>().totalAmount
    CheckoutScreen(
      totalAmount = totalAmount,
      navigateBack = { navController.navigateUp() },
      navigateToPaymentCompleted = { isSuccess, error ->
        navController.navigate(Screen.PaymentCompleted(isSuccess, error))
      },
    )
  }
  composable<Screen.PaymentCompleted> {
    PaymentCompletedScreen(
      navigateBack = {
        navController.navigate(Screen.HomeGraph) {
          launchSingleTop = true
          popUpTo(0) { inclusive = true }
        }
      },
    )
  }
}
