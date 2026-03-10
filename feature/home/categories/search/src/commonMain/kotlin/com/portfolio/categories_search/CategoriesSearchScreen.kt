package com.portfolio.categories_search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.portfolio.categories_search.component.CategorySearchTopBar
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.ProductCard
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.UiState

@Composable
fun CategorySearchScreen(
  category: ProductCategory,
  navigateToDetails: (String) -> Unit,
  navigateBack: () -> Unit,
  filteredProducts: UiState<List<com.nutrisport.shared.domain.Product>>,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
) {
  var searchBarVisible by mutableStateOf(false)

  Scaffold(
    containerColor = Surface,
    topBar = {
      CategorySearchTopBar(
        title = category.title,
        searchBarVisible = searchBarVisible,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onSearchBarVisibilityChange = { searchBarVisible = it },
        onBack = navigateBack,
      )
    },
  ) { padding ->
    filteredProducts.DisplayResult(
      modifier = Modifier.padding(
        top = padding.calculateTopPadding(),
        bottom = padding.calculateBottomPadding(),
      ),
      onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
      onSuccess = { categoryProducts ->
        AnimatedContent(targetState = categoryProducts) { products ->
          if (products.isNotEmpty()) {
            LazyColumn(
              modifier = Modifier.fillMaxSize().padding(all = 12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              items(items = products, key = { it.id }) { product ->
                ProductCard(
                  product = product,
                  onClick = navigateToDetails,
                )
              }
            }
          } else {
            InfoCard(
              image = Resources.Image.Cat,
              title = "Nothing here",
              subtitle = "We couldn't find any product.",
            )
          }
        }
      },
      onError = { message ->
        InfoCard(
          image = Resources.Image.Cat,
          title = "Oops!",
          subtitle = message,
        )
      },
      transitionSpec = fadeIn() togetherWith fadeOut(),
    )
  }
}
