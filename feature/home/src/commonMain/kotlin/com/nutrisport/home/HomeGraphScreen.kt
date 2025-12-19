package com.nutrisport.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nutrisport.home.component.BottomBar
import com.nutrisport.home.domain.BottomBarDestination.Cart
import com.nutrisport.home.domain.BottomBarDestination.Categories
import com.nutrisport.home.domain.BottomBarDestination.ProductsOverview
import com.nutrisport.shared.BebasNeuFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.navigation.Screen
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGraphScreen() {
  val navController = rememberNavController()
  val currentRoute = navController.currentBackStackEntryAsState()

  val selectedDestination by remember {
    derivedStateOf {
      val route = currentRoute.value?.destination?.route.toString()
      when {
        route.contains(ProductsOverview.screen.toString()) -> ProductsOverview
        route.contains(Cart.screen.toString()) -> Cart
        route.contains(Categories.screen.toString()) -> Categories
        else -> ProductsOverview
      }
    }
  }

  Scaffold(
    containerColor = Surface,
    modifier = Modifier.padding(top = 28.dp),
    topBar = {
      CenterAlignedTopAppBar(
        windowInsets = WindowInsets(),
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Surface,
          scrolledContainerColor = Surface,
          navigationIconContentColor = IconPrimary,
          titleContentColor = TextPrimary,
          actionIconContentColor = IconPrimary
        ),
        title = {
          AnimatedContent(
            targetState = selectedDestination
          ) {destination ->
            Text(
              text = destination.title,
              fontFamily = BebasNeuFont(),
              fontSize = FontSize.LARGE,
              color = TextPrimary,
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = {}) {
            Icon(
              painter = painterResource(Resources.Icon.Menu),
              contentDescription = "Menu icon",
              tint = IconPrimary,
            )
          }
        }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(
        top = innerPadding.calculateTopPadding(),
        bottom = innerPadding.calculateBottomPadding(),
      )
    ) {
      NavHost(
        modifier = Modifier.weight(1f),
        navController = navController,
        startDestination = Screen.ProductsOverview
      ) {
        composable<Screen.ProductsOverview> { }
        composable<Screen.Cart> {  }
        composable<Screen.Categories> {  }
      }
      Spacer(modifier = Modifier.height(12.dp))
      Box(modifier = Modifier.padding(12.dp)) {
        BottomBar(
          selected = selectedDestination,
          onSelect = { destination ->
            navController.navigate(destination.screen) {
              launchSingleTop = true
              popUpTo<Screen.ProductsOverview> {
                saveState = true
                inclusive = true
              }
              restoreState = true
            }
          }
        )
      }
    }
  }
}