package com.nutrisport.home.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nutrisport.cart.CartRoute
import com.nutrisport.categories.CategoriesScreen
import com.nutrisport.home.domain.BottomBarDestination
import com.nutrisport.products_overview.ProductsOverviewRoute
import com.nutrisport.shared.navigation.Screen

@Composable
internal fun HomeNavHost(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  selectedDestination: BottomBarDestination,
  cartItemCount: Int,
  navigateToDetails: (String) -> Unit,
  navigateToCategorySearch: (String) -> Unit,
) {
  Column(modifier = modifier.fillMaxSize()) {
    NavHost(
      modifier = Modifier.weight(1f),
      navController = navController,
      startDestination = Screen.ProductsOverview,
    ) {
      composable<Screen.ProductsOverview> {
        ProductsOverviewRoute(goToDetails = navigateToDetails)
      }
      composable<Screen.Cart> {
        CartRoute()
      }
      composable<Screen.Categories> {
        CategoriesScreen(goToCategoriesSearch = navigateToCategorySearch)
      }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Box(modifier = Modifier.padding(all = 12.dp)) {
      BottomBar(
        cartItemCount = cartItemCount,
        selected = selectedDestination,
        onSelect = { destination ->
          navController.navigate(destination.screen) {
            launchSingleTop = true
            popUpTo<Screen.ProductsOverview> {
              saveState = true
              inclusive = false
            }
            restoreState = true
          }
        },
      )
    }
  }
}
