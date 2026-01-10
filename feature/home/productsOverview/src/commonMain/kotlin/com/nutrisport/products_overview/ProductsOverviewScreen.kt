package com.nutrisport.products_overview

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Resources
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.ProductCard
import com.nutrisport.shared.util.DisplayResult
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProductsOverviewScreen() {
  val viewModel = koinViewModel<ProductsOverviewViewModel>()
  val products = viewModel.products.collectAsState()

  products.value.DisplayResult(
    onSuccess = { productList ->
      AnimatedContent(productList) { products ->
        if (products.isNotEmpty()) {
          Column(modifier = Modifier) {
            Text(
              modifier = Modifier
                .alpha(Alpha.HALF)
                .fillMaxWidth(),
              text = "Discounted products",
              fontSize = FontSize.EXTRA_REGULAR,
              color = TextPrimary,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
              modifier = Modifier.padding(horizontal = 12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              items(
                items = products.sortedBy { it.createdAt }.take(3),
                key = { it.id }
              ) { product ->
                ProductCard(
                  product = product,
                  onClick = {},
                )
              }
            }
          }
        } else {
          InfoCard(
            image = Resources.Image.Cat,
            title = "Nothing here",
            subtitle = "Empty product list"
          )
        }
      }
    },
    onLoading = {
      LoadingCard(
        modifier = Modifier.fillMaxSize()
      )
    },
    onError = {
      InfoCard(
        image = Resources.Image.Cat,
        title = "Error",
        subtitle = it
      )
    }
  )
}
