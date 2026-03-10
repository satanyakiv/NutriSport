package com.nutrisport.admin_panel

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.admin_panel.component.AdminPanelTopBar
import com.nutrisport.shared.ButtonPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.ProductCard
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.UiState
import org.jetbrains.compose.resources.painterResource
import rememberMessageBarState

@Composable
fun AdminPanelScreen(
  modifier: Modifier = Modifier,
  goBack: () -> Unit,
  goToManageProduct: (String?) -> Unit,
  products: UiState<List<com.nutrisport.shared.domain.Product>>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
) {
  val messageBarState = rememberMessageBarState()
  var searchBarVisible by rememberSaveable { mutableStateOf(false) }

  Scaffold(
    modifier = modifier,
    containerColor = Surface,
    topBar = {
      AdminPanelTopBar(
        searchBarVisible = searchBarVisible,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onSearchBarVisibilityChange = { searchBarVisible = it },
        onBack = goBack,
      )
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
        },
      )
    },
  ) { padding ->
    ContentWithMessageBar(
      contentBackgroundColor = Surface,
      modifier = Modifier.padding(top = padding.calculateTopPadding()),
      messageBarState = messageBarState,
      errorMaxLines = 2,
      errorContainerColor = SurfaceError,
      errorContentColor = TextWhite,
      successContainerColor = SurfaceBrand,
      successContentColor = TextPrimary,
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
