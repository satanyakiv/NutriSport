package com.nutrisport.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.nutrisport.admin_panel.AdminPanelScreen
import com.nutrisport.auth.component.AuthScreen
import com.nutrisport.details.DetailsScreen
import com.nutrisport.home.HomeGraphScreen
import com.nutrisport.manage_product.ManageProductScreen
import com.nutrisport.profile.ProfileScreen
import com.nutrisport.shared.navigation.Screen

@Composable
fun SetupNavGraph(
  startDestination: Screen = Screen.Auth
) {
  val navController = rememberNavController()
  NavHost(
    navController = navController,
    startDestination = startDestination,
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
        goToAuth = {
          navController.navigate(Screen.Auth) {
            popUpTo<Screen.HomeGraph> { inclusive = true }
          }
        },
        goToProfile = { navController.navigate(Screen.Profile) },
        goToAdminPanel = { navController.navigate(Screen.AdminPanel) },
        goToDetails = { id -> navController.navigate(Screen.Details(id)) }
      )
    }
    composable<Screen.Profile> {
      ProfileScreen(
        goBack = { navController.navigateUp() },
      )
    }
    composable<Screen.AdminPanel> {
      AdminPanelScreen(
        goBack = { navController.navigateUp() },
        goToManageProduct = { id -> navController.navigate(Screen.ManageProduct(id)) }
      )
    }
    composable<Screen.ManageProduct> {
      ManageProductScreen(
        id = it.toRoute<Screen.ManageProduct>().id,
        goBack = { navController.navigateUp() },
      )
    }
    composable<Screen.Details> {
      DetailsScreen(
        goBack = { navController.navigateUp() },
      )
    }
  }
}