package com.nutrisport.admin_panel

import ContentWithMessageBar
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.ButtonPrimary
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.ProductCard
import com.nutrisport.shared.util.DisplayResult
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  modifier: Modifier = Modifier,
  goBack: () -> Unit,
  goToManageProduct: (String?) -> Unit,
) {
  val messageBarState = rememberMessageBarState()
  val viewModel = koinViewModel<AdminPanelVewModel>()
  val products by viewModel.filteredProducts.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  var searchBarVisible by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    containerColor = Surface,
    topBar = {
      AnimatedContent(
        targetState = searchBarVisible
      ) { isVisible ->
        if (isVisible) {
          SearchBar(
            modifier = Modifier
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
              .padding(horizontal = 12.dp)
              .fillMaxWidth(),
            inputField = {
              SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
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
                  IconButton(
                    onClick = {
                      if (searchQuery.isEmpty()) {
                        searchBarVisible = false
                      } else {
                        viewModel.updateSearchQuery("")
                      }
                    }

                  ) {
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
        } else {
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
              IconButton(onClick = { searchBarVisible = true }) {
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
        }
      }
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { goToManageProduct(null) },
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
      messageBarState = messageBarState,
      errorMaxLines = 2,
      errorContainerColor = SurfaceError,
      errorContentColor = TextWhite,
      successContainerColor = SurfaceBrand,
      successContentColor = TextPrimary
    ) {
      products.DisplayResult(
        onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
        onError = { message ->
          InfoCard(
            image = Resources.Image.Cat,
            title = "Oops!",
            subtitle = message,
          )
        },
        onSuccess = { lastProducts ->
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            items(
              items = lastProducts,
              key = { it.id },
            ) { product ->
              ProductCard(
                product = product,
                onClick = { goToManageProduct(product.id) },
              )
            }
          }
        },
      )
    }
  }
}