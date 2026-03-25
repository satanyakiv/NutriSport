package com.nutrisport.details

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.details.mapper.ProductToUiMapper
import com.nutrisport.details.model.DetailsScreenState
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.ConnectivityStatus
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.usecase.ObserveProductWithConnectivityUseCase
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailsViewModel(
  private val observeProductWithConnectivity: ObserveProductWithConnectivityUseCase,
  private val productRepository: ProductRepository,
  private val customerRepository: CustomerRepository,
  private val productToUiMapper: ProductToUiMapper,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val productId = savedStateHandle.get<String>("id").orEmpty()
  private var wasOffline = false

  private val _screenState = MutableStateFlow(DetailsScreenState())
  val screenState: StateFlow<DetailsScreenState> = _screenState.asStateFlow()

  var quantity by mutableStateOf(1)
    private set

  var selectedFlavor: String? by mutableStateOf(null)
    private set

  init {
    observeProductAndConnectivity()
  }

  private fun observeProductAndConnectivity() {
    observeProductWithConnectivity(productId)
      .onEach { (productResult, connectivity) ->
        val reconnected = wasOffline && connectivity == ConnectivityStatus.Available
        wasOffline = connectivity == ConnectivityStatus.Unavailable

        _screenState.update { current ->
          current.copy(
            product = UiState.Content(productResult.map { productToUiMapper.map(it) }),
            connectivity = connectivity,
            showReconnectedPrompt = reconnected || current.showReconnectedPrompt,
          )
        }
      }
      .launchIn(viewModelScope)
  }

  fun refresh() {
    viewModelScope.launch {
      _screenState.update { it.copy(showReconnectedPrompt = false) }
      productRepository.refreshProductById(productId)
    }
  }

  fun dismissReconnectedPrompt() {
    _screenState.update { it.copy(showReconnectedPrompt = false) }
  }

  fun acknowledgePriceChange() {
    viewModelScope.launch {
      productRepository.acknowledgePriceChange(productId)
    }
  }

  fun updateQuantity(value: Int) {
    quantity = value
  }

  fun updateFlavor(value: String) {
    selectedFlavor = value
  }

  fun addItemToCart(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      val id = savedStateHandle.get<String>("id")
      if (id != null) {
        customerRepository.addItemToCart(
          cartItem = CartItem(
            productId = id,
            flavor = selectedFlavor,
            quantity = quantity,
          ),
        ).fold(
          ifLeft = { error -> onError(error.message) },
          ifRight = { onSuccess() },
        )
      } else {
        onError("Product id is not found.")
      }
    }
  }
}
