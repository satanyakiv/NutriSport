package com.nutrisport.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.details.mapper.ProductToUiMapper
import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailsViewModel(
  private val productRepository: ProductRepository,
  private val customerRepository: CustomerRepository,
  private val productToUiMapper: ProductToUiMapper,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  val product = productRepository.readProductByIdFlow(
    savedStateHandle.get<String>("id").orEmpty()
  ).map<_, UiState<ProductUi>> { result ->
    UiState.Content(result.map { productToUiMapper.map(it) })
  }.onStart { emit(UiState.Loading) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading
    )

  var quantity by mutableStateOf(1)
    private set

  var selectedFlavor: String? by mutableStateOf(null)
    private set

  fun updateQuantity(value: Int) {
    quantity = value
  }

  fun updateFlavor(value: String) {
    selectedFlavor = value
  }

  fun addItemToCart(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val productId = savedStateHandle.get<String>("id")
      if (productId != null) {
        customerRepository.addItemToCart(
          cartItem = CartItem(
            productId = productId,
            flavor = selectedFlavor,
            quantity = quantity
          ),
        ).fold(
          ifLeft = { error -> onError(error.message) },
          ifRight = { onSuccess() }
        )
      } else {
        onError("Product id is not found.")
      }
    }
  }
}
