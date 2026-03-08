package com.nutrisport.cart


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import kotlinx.coroutines.launch

class CartViewModel(
  private val customerRepository: CustomerRepository,
  private val observeEnrichedCartUseCase: ObserveEnrichedCartUseCase,
) : ViewModel() {
  val cartItemsWithProducts = observeEnrichedCartUseCase()

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
        onSuccess = onSuccess,
        onError = onError
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
        onSuccess = onSuccess,
        onError = onError
      )
    }
  }
}
