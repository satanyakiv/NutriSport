package com.nutrisport.admin_panel

import ContentWithMessageBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.ButtonPrimary
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import org.jetbrains.compose.resources.painterResource
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  modifier: Modifier = Modifier,
  goBack: () -> Unit,
) {
  val messageBarState = rememberMessageBarState()

  Scaffold(
    containerColor = Surface,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Admin Panel",
            fontFamily = BebasNeueFont(),
            fontSize = FontSize.LARGE,
            color = TextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = goBack) {
            Icon(
              painter = painterResource(Resources.Icon.BackArrow),
              contentDescription = "Back Arrow icon",
              tint = IconPrimary
            )
          }
        },
        actions = {
          IconButton(onClick = goBack) {
            Icon(
              painter = painterResource(Resources.Icon.Search),
              contentDescription = "Search icon",
              tint = IconPrimary
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Surface,
          scrolledContainerColor = Surface,
          navigationIconContentColor = IconPrimary,
          titleContentColor = TextPrimary,
          actionIconContentColor = IconPrimary
        ),
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {},
        containerColor = ButtonPrimary,
        contentColor = TextPrimary,
        content = {
          Icon(
            painter = painterResource(Resources.Icon.Plus),
            contentDescription = "Add product icon",
          )
        }
      )
    }
  ) { padding ->
    ContentWithMessageBar(
      contentBackgroundColor = Surface,
      modifier = Modifier
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

    }
  }
}