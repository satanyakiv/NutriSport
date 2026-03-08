package com.nutrisport.navigation

import CategorySearchScreen
import androidx.compose.runtime.Composable
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
    startDestination = startDestination
  ) {
    composable<Screen.Auth> {
      AuthScreen(
        goToHome = {
          navController.navigate(Screen.HomeGraph) {
            popUpTo<Screen.Auth> { inclusive = true }
          }
        }
      )
    }
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
        }
      )
    }
    composable<Screen.Profile> {
      ProfileScreen(
        goBack = {
          navController.navigateUp()
        }
      )
    }
    composable<Screen.AdminPanel> {
      AdminPanelScreen(
        goBack = {
          navController.navigateUp()
        },
        goToManageProduct = { id ->
          navController.navigate(Screen.ManageProduct(id = id))
        }
      )
    }
    composable<Screen.ManageProduct> {
      val id = it.toRoute<Screen.ManageProduct>().id
      ManageProductScreen(
        id = id,
        goBack = {
          navController.navigateUp()
        }
      )
    }
    composable<Screen.Details> {
      DetailsScreen(
        goBack = {
          navController.navigateUp()
        }
      )
    }
    composable<Screen.CategorySearch> {
      val category = ProductCategory.valueOf(it.toRoute<Screen.CategorySearch>().category)
      CategorySearchScreen(
        category = category,
        navigateToDetails = { id ->
          navController.navigate(Screen.Details(id))
        },
        navigateBack = {
          navController.navigateUp()
        }
      )
    }
    composable<Screen.Checkout> {
      val totalAmount = it.toRoute<Screen.Checkout>().totalAmount
      CheckoutScreen(
        totalAmount = totalAmount,
        navigateBack = {
          navController.navigateUp()
        },
        navigateToPaymentCompleted = { isSuccess, error ->
          navController.navigate(Screen.PaymentCompleted(isSuccess, error))
        }
      )
    }
    composable<Screen.PaymentCompleted> {
      PaymentCompletedScreen(
        navigateBack = {
          navController.navigate(Screen.HomeGraph) {
            launchSingleTop = true
            // Clear backstack completely
            popUpTo(0) { inclusive = true }
          }
        }
      )
    }
  }
}
