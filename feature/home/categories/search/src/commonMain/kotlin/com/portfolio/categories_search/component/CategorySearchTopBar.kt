package com.portfolio.categories_search.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.BorderIdle
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CategorySearchTopBar(
  title: String,
  searchBarVisible: Boolean,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onSearchBarVisibilityChange: (Boolean) -> Unit,
  onBack: () -> Unit,
) {
  AnimatedContent(targetState = searchBarVisible) { visible ->
    if (visible) {
      SearchBar(
        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
        inputField = {
          SearchBarDefaults.InputField(
            modifier = Modifier.fillMaxWidth(),
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            expanded = false,
            onExpandedChange = {},
            onSearch = {},
            placeholder = {
              Text(
                text = "Search here",
                fontSize = FontSize.REGULAR,
                color = TextPrimary,
              )
            },
            trailingIcon = {
              IconButton(
                modifier = Modifier.size(14.dp),
                onClick = {
                  if (searchQuery.isNotEmpty()) {
                    onSearchQueryChange("")
                  } else {
                    onSearchBarVisibilityChange(false)
                  }
                },
              ) {
                Icon(
                  painter = painterResource(Resources.Icon.Close),
                  contentDescription = "Close icon",
                )
              }
            },
          )
        },
        colors = SearchBarColors(
          containerColor = SurfaceLighter,
          dividerColor = BorderIdle,
        ),
        expanded = false,
        onExpandedChange = {},
        content = {},
      )
    } else {
      TopAppBar(
        title = {
          Text(
            text = title,
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
          IconButton(onClick = { onSearchBarVisibilityChange(true) }) {
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
  }
}
