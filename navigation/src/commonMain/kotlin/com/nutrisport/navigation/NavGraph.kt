package com.nutrisport.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nutrisport.admin_panel.AdminPanelRoute
import com.nutrisport.auth.component.AuthRoute
import com.nutrisport.checkout.CheckoutRoute
import com.nutrisport.details.DetailsRoute
import com.nutrisport.home.HomeGraphRoute
import com.nutrisport.manage_product.ManageProductRoute
import com.nutrisport.profile.ProfileRoute
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.navigation.Screen
import com.portfolio.categories_search.CategoriesSearchRoute
import com.portfolio.payment_completed.PaymentCompletedRoute

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
    AuthRoute(
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
    HomeGraphRoute(
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
    ProfileRoute(
      goBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.adminDestinations(navController: NavController) {
  composable<Screen.AdminPanel> {
    AdminPanelRoute(
      goBack = { navController.navigateUp() },
      goToManageProduct = { id ->
        navController.navigate(Screen.ManageProduct(id = id))
      },
    )
  }
  composable<Screen.ManageProduct> {
    val id = it.toRoute<Screen.ManageProduct>().id
    ManageProductRoute(
      id = id,
      goBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.detailsDestination(navController: NavController) {
  composable<Screen.Details> {
    DetailsRoute(
      goBack = { navController.navigateUp() },
    )
  }
}

private fun NavGraphBuilder.categorySearchDestination(navController: NavController) {
  composable<Screen.CategorySearch> {
    val category = ProductCategory.valueOf(it.toRoute<Screen.CategorySearch>().category)
    CategoriesSearchRoute(
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
    CheckoutRoute(
      totalAmount = totalAmount,
      navigateBack = { navController.navigateUp() },
      navigateToPaymentCompleted = { isSuccess, error ->
        navController.navigate(Screen.PaymentCompleted(isSuccess, error))
      },
    )
  }
  composable<Screen.PaymentCompleted> {
    PaymentCompletedRoute(
      navigateBack = {
        navController.navigate(Screen.HomeGraph) {
          launchSingleTop = true
          popUpTo(0) { inclusive = true }
        }
      },
    )
  }
}
