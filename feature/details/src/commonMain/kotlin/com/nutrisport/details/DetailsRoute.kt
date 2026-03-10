package com.nutrisport.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailsRoute(goBack: () -> Unit) {
    val viewModel = koinViewModel<DetailsViewModel>()
    val product by viewModel.product.collectAsState()

    DetailsScreen(
        goBack = goBack,
        product = product,
        quantity = viewModel.quantity,
        selectedFlavor = viewModel.selectedFlavor,
        onUpdateQuantity = viewModel::updateQuantity,
        onUpdateFlavor = viewModel::updateFlavor,
        onAddItemToCart = viewModel::addItemToCart,
    )
}
