package com.nutrisport.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.nutrisport.home.domain.BottomBarDestination
import com.nutrisport.home.domain.CustomDrawerState
import com.nutrisport.home.domain.isOpened
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeTopBar(
  selectedDestination: BottomBarDestination,
  drawerState: CustomDrawerState,
  showCheckoutAction: Boolean,
  onDrawerToggle: () -> Unit,
  onCheckoutClick: () -> Unit,
) {
  CenterAlignedTopAppBar(
    title = {
      AnimatedContent(targetState = selectedDestination) { destination ->
        Text(
          text = destination.title,
          fontFamily = BebasNeueFont(),
          fontSize = FontSize.LARGE,
          color = TextPrimary,
        )
      }
    },
    actions = {
      AnimatedVisibility(
        visible = selectedDestination == BottomBarDestination.Cart
      ) {
        if (showCheckoutAction) {
          IconButton(onClick = onCheckoutClick) {
            Icon(
              painter = painterResource(Resources.Icon.RightArrow),
              contentDescription = "Right icon",
              tint = IconPrimary,
            )
          }
        }
      }
    },
    navigationIcon = {
      AnimatedContent(targetState = drawerState) { drawer ->
        val icon = if (drawer.isOpened()) Resources.Icon.Close else Resources.Icon.Menu
        val description = if (drawer.isOpened()) "Close icon" else "Menu icon"
        IconButton(onClick = onDrawerToggle) {
          Icon(
            painter = painterResource(icon),
            contentDescription = description,
            tint = IconPrimary,
          )
        }
      }
    },
    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
      containerColor = Surface,
      scrolledContainerColor = Surface,
      navigationIconContentColor = IconPrimary,
      titleContentColor = TextPrimary,
      actionIconContentColor = IconPrimary,
    ),
  )
}
