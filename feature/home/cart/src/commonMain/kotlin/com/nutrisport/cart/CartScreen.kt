package com.nutrisport.cart

import ContentWithMessageBar
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.cart.component.CartItemCard
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.InfoCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.UiState
import rememberMessageBarState

@Composable
fun CartScreen(
  cartItems: UiState<List<com.nutrisport.cart.model.CartItemUi>>,
  onUpdateQuantity: (String, Int, () -> Unit, (String) -> Unit) -> Unit,
  onDeleteItem: (String, () -> Unit, (String) -> Unit) -> Unit,
) {
  val messageBarState = rememberMessageBarState()

  ContentWithMessageBar(
    contentBackgroundColor = Surface,
    messageBarState = messageBarState,
    errorMaxLines = 2,
    errorContainerColor = SurfaceError,
    errorContentColor = TextWhite,
    successContainerColor = SurfaceBrand,
    successContentColor = TextPrimary
  ) {
    cartItems.DisplayResult(
      onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
      onSuccess = { data ->
        if (data.isNotEmpty()) {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(
              items = data,
              key = { it.cartItemId }
            ) { cartItemUi ->
              CartItemCard(
                cartItemUi = cartItemUi,
                onMinusClick = { quantity ->
                  onUpdateQuantity(
                    cartItemUi.cartItemId,
                    quantity,
                    {},
                    { messageBarState.addError(it) },
                  )
                },
                onPlusClick = { quantity ->
                  onUpdateQuantity(
                    cartItemUi.cartItemId,
                    quantity,
                    {},
                    { messageBarState.addError(it) },
                  )
                },
                onDeleteClick = {
                  onDeleteItem(
                    cartItemUi.cartItemId,
                    {},
                    { messageBarState.addError(it) },
                  )
                }
              )
            }
          }
        } else {
          InfoCard(
            image = Resources.Image.ShoppingCart,
            title = "Empty Cart",
            subtitle = "Check some of our products."
          )
        }
      },
      onError = { message ->
        InfoCard(
          image = Resources.Image.Cat,
          title = "Oops!",
          subtitle = message
        )
      },
      transitionSpec = fadeIn() togetherWith fadeOut()
    )
  }
}
