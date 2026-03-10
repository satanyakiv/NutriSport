package com.nutrisport.admin_panel.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AdminPanelTopBar(
  searchBarVisible: Boolean,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onSearchBarVisibilityChange: (Boolean) -> Unit,
  onBack: () -> Unit,
) {
  AnimatedContent(targetState = searchBarVisible) { isVisible ->
    if (isVisible) {
      AdminSearchBar(
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onClose = {
          if (searchQuery.isEmpty()) {
            onSearchBarVisibilityChange(false)
          } else {
            onSearchQueryChange("")
          }
        },
      )
    } else {
      AdminToolbar(
        onBack = onBack,
        onSearchClick = { onSearchBarVisibilityChange(true) },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminSearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onClose: () -> Unit,
) {
  SearchBar(
    modifier = Modifier
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
      .padding(horizontal = 12.dp)
      .fillMaxWidth(),
    inputField = {
      SearchBarDefaults.InputField(
        modifier = Modifier.fillMaxWidth(),
        query = searchQuery,
        onQueryChange = onSearchQueryChange,
        onSearch = { },
        expanded = false,
        onExpandedChange = { },
        colors = inputFieldColors(
          focusedContainerColor = SurfaceLighter,
          unfocusedContainerColor = SurfaceLighter,
          disabledContainerColor = SurfaceLighter,
        ),
        placeholder = {
          Text(
            text = "Search",
            fontSize = FontSize.REGULAR,
            color = TextPrimary,
          )
        },
        trailingIcon = {
          IconButton(onClick = onClose) {
            Icon(
              modifier = Modifier.size(14.dp),
              painter = painterResource(Resources.Icon.Close),
              contentDescription = "Close icon",
              tint = IconPrimary,
            )
          }
        },
      )
    },
    state = rememberSearchBarState(),
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminToolbar(
  onBack: () -> Unit,
  onSearchClick: () -> Unit,
) {
  TopAppBar(
    title = {
      Text(
        text = "Admin Panel",
        fontFamily = BebasNeueFont(),
        fontSize = FontSize.LARGE,
        color = TextPrimary,
      )
    },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(
          painter = painterResource(Resources.Icon.BackArrow),
          contentDescription = "Back Arrow icon",
          tint = IconPrimary,
        )
      }
    },
    actions = {
      IconButton(onClick = onSearchClick) {
        Icon(
          painter = painterResource(Resources.Icon.Search),
          contentDescription = "Search icon",
          tint = IconPrimary,
        )
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
