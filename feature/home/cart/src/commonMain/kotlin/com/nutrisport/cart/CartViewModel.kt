package com.nutrisport.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CartViewModel(
    private val customerRepository: CustomerRepository,
    private val observeEnrichedCartUseCase: ObserveEnrichedCartUseCase,
) : ViewModel() {
    val cartItemsWithProducts = observeEnrichedCartUseCase()
        .map<_, UiState<List<Pair<CartItem, Product>>>> { UiState.Content(it) }
        .onStart { emit(UiState.Loading) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    fun updateCartItemQuantity(
        id: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            customerRepository.updateCartItemQuantity(
                id = id,
                quantity = quantity,
            ).fold(
                ifLeft = { error -> onError(error.message) },
                ifRight = { onSuccess() },
            )
        }
    }

    fun deleteCartItem(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            customerRepository.deleteCartItem(
                id = id,
            ).fold(
                ifLeft = { error -> onError(error.message) },
                ifRight = { onSuccess() },
            )
        }
    }
}
