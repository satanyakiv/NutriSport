package com.nutrisport.home

import ContentWithMessageBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nutrisport.home.component.BottomBar
import com.nutrisport.home.component.CustomDrawer
import com.nutrisport.home.domain.BottomBarDestination.Cart
import com.nutrisport.home.domain.BottomBarDestination.Categories
import com.nutrisport.home.domain.BottomBarDestination.ProductsOverview
import com.nutrisport.home.domain.CustomDrawerState
import com.nutrisport.home.domain.isOpened
import com.nutrisport.home.domain.opposite
import com.nutrisport.products_overview.ProductsOverviewScreen
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.navigation.Screen
import com.nutrisport.shared.util.getScreenWidth
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeGraphScreen(
  goToAuth: () -> Unit,
  goToProfile: () -> Unit,
  goToAdminPanel: () -> Unit,
  goToDetails:(String) -> Unit,
) {
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

  val screenWidth = remember { getScreenWidth() }
  var drawerState by remember {
    mutableStateOf(CustomDrawerState.Closed)
  }
  val animatedBackground by animateColorAsState(
    targetValue = if (drawerState.isOpened()) SurfaceLighter else Surface,
  )
  val offsetValue by remember { derivedStateOf { (screenWidth / 1.5).dp } }
  val animatedOffset by animateDpAsState(
    targetValue = if (drawerState.isOpened()) offsetValue else 0.dp,
  )
  val animateScale by animateFloatAsState(
    targetValue = if (drawerState.isOpened()) 0.9f else 1f,
  )
  val animatedRadius by animateDpAsState(
    targetValue = if (drawerState.isOpened()) 20.dp else 0.dp,
  )

  val viewModel = koinViewModel<HomeGraphViewModel>()
  val customer by viewModel.customer.collectAsState()
  val messageBarState = rememberMessageBarState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(animatedBackground)
      .systemBarsPadding()
  ) {
    CustomDrawer(
      customer = customer,
      onProfileClick = { goToProfile() },
      onContactUsClick = { },
      onSignOutClick = {
        viewModel.signOut(
          onSuccess = { goToAuth() },
          onError = { message -> messageBarState.addError(message) }
        )
      },
      onAdminPanelClick = goToAdminPanel,
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(animatedRadius))
        .offset(x = animatedOffset)
        .scale(scale = animateScale)
        .shadow(
          elevation = 20.dp,
          shape = RoundedCornerShape(animatedRadius),
          spotColor = Color.Black.copy(alpha = Alpha.TEN_PERCENT),
          ambientColor = Color.Black.copy(alpha = Alpha.TEN_PERCENT),
        )
    ) {
      Scaffold(containerColor = Surface, topBar = {
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
            ) { destination ->
              Text(
                text = destination.title,
                fontFamily = BebasNeueFont(),
                fontSize = FontSize.LARGE,
                color = TextPrimary,
              )
            }
          },
          navigationIcon = {
            AnimatedContent(
              targetState = drawerState,
            ) {
              IconButton(
                onClick = {
                  drawerState = drawerState.opposite()
                }
              ) {
                val icon = if (it.isOpened()) Resources.Icon.Close else Resources.Icon.Menu
                Icon(
                  painter = painterResource(icon),
                  contentDescription = "Navigation drawer icon",
                  tint = IconPrimary,
                )
              }
            }
          }
        )
      }) { innerPadding ->
        ContentWithMessageBar(
          messageBarState = messageBarState,
          errorMaxLines = 2,
          contentBackgroundColor = Surface,
          modifier = Modifier
            .fillMaxSize()
            .padding(
              top = innerPadding.calculateTopPadding(),
              bottom = innerPadding.calculateBottomPadding(),
            )
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
          ) {
            NavHost(
              modifier = Modifier.weight(1f),
              navController = navController,
              startDestination = Screen.ProductsOverview
            ) {
              composable<Screen.ProductsOverview> {
                ProductsOverviewScreen(
                  goToDetails = { id -> goToDetails(id) }
                )
              }
              composable<Screen.Cart> { }
              composable<Screen.Categories> { }
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
    }
  }
}