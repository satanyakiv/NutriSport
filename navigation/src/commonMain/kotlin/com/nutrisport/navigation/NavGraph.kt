package com.nutrisport.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.nutrisport.admin_panel.AdminPanelRoute
import com.nutrisport.auth.component.AuthRoute
import com.nutrisport.checkout.CheckoutRoute
import com.nutrisport.details.DetailsRoute
import com.nutrisport.home.HomeGraphRoute
import com.nutrisport.manage_product.ManageProductRoute
import com.nutrisport.navigation.debug.DebugToolkit
import com.nutrisport.profile.ProfileRoute
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.navigation.NavigationCommand
import com.nutrisport.shared.domain.navigation.Router
import com.nutrisport.shared.navigation.Screen
import com.portfolio.categories_search.CategoriesSearchRoute
import com.portfolio.payment_completed.PaymentCompletedRoute
import org.koin.compose.koinInject

/**
 * Root navigation graph. Owns the bridging from [Router.commands] to the live
 * `NavController`. ViewModels emit [NavigationCommand]s through the [Router] and
 * the collector below translates each into a framework call, so no feature module
 * ever touches the `NavController` directly. See `.claude/rules/navigation.md`.
 *
 * The bottom-bar switching inside `HomeGraph` is a SEPARATE, nested `HomeNavHost`
 * and intentionally does NOT go through the [Router] — it is local tab state, not
 * app-level navigation.
 */
@Composable
fun SetupNavGraph(startDestination: Screen = Screen.Auth) {
  val debugToolkit = koinInject<DebugToolkit>()
  val router = koinInject<Router>()
  val navController = debugToolkit.rememberNavController()

  LaunchedEffect(navController, router) {
    router.commands.collect { command ->
      when (command) {
        is NavigationCommand.NavigateTo -> navController.navigate(command.destination)
        is NavigationCommand.Replace -> navController.navigate(command.destination) {
          launchSingleTop = true
          popUpTo(0) { inclusive = true }
        }
        NavigationCommand.Back -> navController.popBackStack()
        is NavigationCommand.PopUpTo -> navController.popBackStack(
          route = command.destination,
          inclusive = command.inclusive,
        )
        NavigationCommand.PopToRoot -> navController.popBackStack(
          navController.graph.findStartDestination().id,
          inclusive = false,
        )
      }
    }
  }

  NavHost(
    navController = navController,
    startDestination = startDestination,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None },
    popEnterTransition = { EnterTransition.None },
    popExitTransition = { ExitTransition.None },
  ) {
    composable<Screen.Auth> { AuthRoute() }
    composable<Screen.HomeGraph> { HomeGraphRoute() }
    composable<Screen.Profile> { ProfileRoute() }
    composable<Screen.AdminPanel> { AdminPanelRoute() }
    composable<Screen.ManageProduct> {
      ManageProductRoute(id = it.toRoute<Screen.ManageProduct>().id)
    }
    composable<Screen.Details> { DetailsRoute() }
    composable<Screen.CategorySearch> {
      val category = ProductCategory.valueOf(it.toRoute<Screen.CategorySearch>().category)
      CategoriesSearchRoute(category = category)
    }
    composable<Screen.Checkout> {
      CheckoutRoute(totalAmount = it.toRoute<Screen.Checkout>().totalAmount)
    }
    composable<Screen.PaymentCompleted> { PaymentCompletedRoute() }
  }
}
