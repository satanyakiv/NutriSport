package com.nutrisport.details

import ContentWithMessageBar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.details.component.DetailsTopBar
import com.nutrisport.details.component.FlavorChip
import com.nutrisport.details.component.ProductDetailsContent
import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.PrimaryButton
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.UiState
import rememberMessageBarState

@Composable
fun DetailsScreen(
  goBack: () -> Unit,
  product: UiState<ProductUi>,
  quantity: Int,
  selectedFlavor: String?,
  onUpdateQuantity: (Int) -> Unit,
  onUpdateFlavor: (String) -> Unit,
  onAddItemToCart: (() -> Unit, (String) -> Unit) -> Unit,
) {
  val messageBarState = rememberMessageBarState()

  Scaffold(
    containerColor = Surface,
    topBar = {
      DetailsTopBar(
        quantity = quantity,
        onBack = goBack,
        onUpdateQuantity = onUpdateQuantity,
      )
    },
  ) { padding ->
    product.DisplayResult(
      onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
      onSuccess = { selectedProduct ->
        ContentWithMessageBar(
          contentBackgroundColor = Surface,
          modifier = Modifier.padding(
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding(),
          ),
          messageBarState = messageBarState,
          errorMaxLines = 2,
          errorContainerColor = SurfaceError,
          errorContentColor = TextWhite,
          successContainerColor = SurfaceBrand,
          successContentColor = TextPrimary,
        ) {
          Column {
            Column(
              modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp),
            ) {
              ProductDetailsContent(product = selectedProduct)
            }
            BottomSection(
              product = selectedProduct,
              selectedFlavor = selectedFlavor,
              onUpdateFlavor = onUpdateFlavor,
              onAddToCart = {
                onAddItemToCart(
                  { messageBarState.addSuccess("Product added to cart.") },
                  { message -> messageBarState.addError(message) },
                )
              },
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
    )
  }
}

@Composable
private fun BottomSection(
  product: ProductUi,
  selectedFlavor: String?,
  onUpdateFlavor: (String) -> Unit,
  onAddToCart: () -> Unit,
) {
  Column(
    modifier = Modifier
      .background(if (product.hasFlavors) SurfaceLighter else Surface)
      .padding(all = 24.dp),
  ) {
    if (product.hasFlavors) {
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.Center,
      ) {
        product.flavors.forEach { flavor ->
          FlavorChip(
            flavor = flavor,
            isSelected = selectedFlavor == flavor,
            onClick = { onUpdateFlavor(flavor) },
          )
          Spacer(modifier = Modifier.width(8.dp))
        }
      }
      Spacer(modifier = Modifier.height(24.dp))
    }
    PrimaryButton(
      icon = Resources.Icon.ShoppingCart,
      text = "Add to Cart",
      enabled = if (product.hasFlavors) selectedFlavor != null else true,
      onClick = onAddToCart,
    )
  }
}
