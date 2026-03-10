package com.nutrisport.cart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CartRoute() {
    val viewModel = koinViewModel<CartViewModel>()
    val cartItems by viewModel.cartItems.collectAsState()

    CartScreen(
        cartItems = cartItems,
        onUpdateQuantity = viewModel::updateCartItemQuantity,
        onDeleteItem = viewModel::deleteCartItem,
    )
}
