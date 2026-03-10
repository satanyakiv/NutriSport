package com.nutrisport.home

import ContentWithMessageBar
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nutrisport.home.component.CustomDrawer
import com.nutrisport.home.component.HomeNavHost
import com.nutrisport.home.component.HomeTopBar
import com.nutrisport.home.domain.BottomBarDestination
import com.nutrisport.home.domain.CustomDrawerState
import com.nutrisport.home.domain.isOpened
import com.nutrisport.home.domain.opposite
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.util.UiState
import com.nutrisport.shared.util.getScreenWidth
import com.nutrisport.shared.util.orZero
import rememberMessageBarState

@Composable
fun HomeGraphScreen(
  navigateToAuth: () -> Unit,
  navigateToProfile: () -> Unit,
  navigateToAdminPanel: () -> Unit,
  navigateToDetails: (String) -> Unit,
  navigateToCategorySearch: (String) -> Unit,
  navigateToCheckout: (Double) -> Unit,
  customer: UiState<com.nutrisport.shared.domain.Customer>,
  totalAmount: UiState<Double>,
  onSignOut: (() -> Unit, (String) -> Unit) -> Unit,
) {
  val navController = rememberNavController()
  val currentRoute = navController.currentBackStackEntryAsState()
  val selectedDestination by remember {
    derivedStateOf {
      val route = currentRoute.value?.destination?.route.toString()
      when {
        route.contains(BottomBarDestination.ProductsOverview.screen.toString()) -> BottomBarDestination.ProductsOverview
        route.contains(BottomBarDestination.Cart.screen.toString()) -> BottomBarDestination.Cart
        route.contains(BottomBarDestination.Categories.screen.toString()) -> BottomBarDestination.Categories
        else -> BottomBarDestination.ProductsOverview
      }
    }
  }

  val screenWidth = remember { getScreenWidth() }
  var drawerState by remember { mutableStateOf(CustomDrawerState.Closed) }

  val offsetValue by remember { derivedStateOf { (screenWidth / 1.5).dp } }
  val animatedOffset by animateDpAsState(
    targetValue = if (drawerState.isOpened()) offsetValue else 0.dp
  )

  val animatedBackground by animateColorAsState(
    targetValue = if (drawerState.isOpened()) SurfaceLighter else Surface
  )

  val animatedScale by animateFloatAsState(
    targetValue = if (drawerState.isOpened()) 0.9f else 1f
  )

  val animatedRadius by animateDpAsState(
    targetValue = if (drawerState.isOpened()) 20.dp else 0.dp
  )

  val messageBarState = rememberMessageBarState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(animatedBackground)
      .systemBarsPadding()
  ) {
    CustomDrawer(
      customer = customer,
      onProfileClick = navigateToProfile,
      onContactUsClick = {},
      onSignOutClick = {
        onSignOut(
          navigateToAuth,
          { message -> messageBarState.addError(message) },
        )
      },
      onAdminPanelClick = navigateToAdminPanel
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(size = animatedRadius))
        .offset(x = animatedOffset)
        .scale(scale = animatedScale)
        .shadow(
          elevation = 20.dp,
          shape = RoundedCornerShape(size = animatedRadius),
          ambientColor = Color.Black.copy(alpha = Alpha.DISABLED),
          spotColor = Color.Black.copy(alpha = Alpha.DISABLED)
        )
    ) {
      Scaffold(
        containerColor = Surface,
        topBar = {
          HomeTopBar(
            selectedDestination = selectedDestination,
            drawerState = drawerState,
            showCheckoutAction = customer.getSuccessDataOrNull()?.cart?.isNotEmpty() == true,
            onDrawerToggle = { drawerState = drawerState.opposite() },
            onCheckoutClick = {
              val total = totalAmount.getSuccessDataOrNull()
              val error = totalAmount.getErrorMessageOrNull()
              if (total != null) {
                navigateToCheckout(total)
              } else if (error != null) {
                messageBarState.addError("Error while calculating a total amount: $error")
              }
            },
          )
        }
      ) { padding ->
        ContentWithMessageBar(
          contentBackgroundColor = Surface,
          modifier = Modifier
            .fillMaxSize()
            .padding(
              top = padding.calculateTopPadding(),
              bottom = padding.calculateBottomPadding()
            ),
          messageBarState = messageBarState,
          errorMaxLines = 2,
          errorContainerColor = SurfaceError,
          errorContentColor = TextWhite,
          successContainerColor = SurfaceBrand,
          successContentColor = TextPrimary
        ) {
          HomeNavHost(
            navController = navController,
            selectedDestination = selectedDestination,
            cartItemCount = customer.getSuccessDataOrNull()?.cart?.size.orZero(),
            navigateToDetails = navigateToDetails,
            navigateToCategorySearch = navigateToCategorySearch,
          )
        }
      }
    }
  }
}
