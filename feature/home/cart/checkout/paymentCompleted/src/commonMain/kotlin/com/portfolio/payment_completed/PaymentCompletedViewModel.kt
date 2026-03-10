package com.portfolio.payment_completed


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Order
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.orZero
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaymentViewModel(
  private val savedStateHandle: SavedStateHandle,
  private val customerRepository: CustomerRepository,
  private val orderRepository: OrderRepository,
  private val productRepository: ProductRepository,
) : ViewModel() {
  var screenState: UiState<Unit> by mutableStateOf(UiState.Loading)

  private val customer = customerRepository.readCustomerFlow()
    .map { UiState.Content(it) }
    .onStart<UiState<com.nutrisport.shared.domain.Customer>> { emit(UiState.Loading) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading
    )

  @OptIn(ExperimentalCoroutinesApi::class)
  private val totalAmount = customer.flatMapLatest { customerState ->
    val customerData = customerState.getSuccessDataOrNull()
    when {
      customerData != null -> {
        val cartItems = customerData.cart
        val productIds = cartItems.map { it.productId }

        if (productIds.isEmpty()) {
          flowOf(UiState.Content(Either.Right(0.0)))
        } else {
          productRepository.readProductsByIdsFlow(productIds)
            .map { productsResult ->
              productsResult.fold(
                ifLeft = { error ->
                  UiState.Content(Either.Left(error))
                },
                ifRight = { products ->
                  UiState.Content(
                    Either.Right(
                      calculateTotalPrice(
                        cartItems = cartItems,
                        products = products
                      )
                    )
                  )
                }
              )
            }
        }
      }

      customerState.getErrorMessageOrNull() != null -> {
        flowOf(UiState.Content(Either.Left(AppError.Unknown(customerState.getErrorMessageOrNull()!!))))
      }

      else -> flowOf(UiState.Loading)
    }
  }

  init {
    viewModelScope.launch {
      totalAmount.collectLatest { amount ->
        val amountData = amount.getSuccessDataOrNull()
        val amountError = amount.getErrorMessageOrNull()
        if (amountData != null) {
          val isSuccess = savedStateHandle.get<Boolean>("isSuccess")
          val error = savedStateHandle.get<String>("error")
          val token = savedStateHandle.get<String>("token")

          if (isSuccess != null) {
            screenState = UiState.Content(Either.Right(Unit))
            if (token != null) {
              createTheOrder(
                totalAmount = amountData,
                token = token,
                onError = { message ->
                  screenState = UiState.Content(Either.Left(AppError.Unknown(message)))
                }
              )
            }
          } else if (error != null) {
            screenState = UiState.Content(Either.Left(AppError.Unknown(error)))
          } else {
            screenState = UiState.Content(
              Either.Left(AppError.Unknown("Unknown error. Contact us at: example@gmail.com"))
            )
          }
        } else if (amountError != null) {
          screenState = UiState.Content(Either.Left(AppError.Unknown(amountError)))
        }
      }
    }
  }

  private fun createTheOrder(
    totalAmount: Double,
    token: String,
    onError: (String) -> Unit,
  ) {
    val customerData = customer.value.getSuccessDataOrNull()
    if (customerData != null) {
      val customerId = customerData.id
      viewModelScope.launch(Dispatchers.IO) {
        orderRepository.createTheOrder(
          order = Order(
            customerId = customerId,
            items = customerData.cart,
            totalAmount = totalAmount,
            token = token
          ),
        ).fold(
          ifLeft = { error -> onError(error.message) },
          ifRight = { Napier.d("ORDER SUCCESSFULLY CREATED!") }
        )
      }
    } else {
      val errorMsg = customer.value.getErrorMessageOrNull()
      if (errorMsg != null) onError(errorMsg)
    }
  }

  fun calculateTotalPrice(cartItems: List<CartItem>, products: List<Product>): Double {
    return cartItems.sumOf { cartItem ->
      val product = products.find { it.id == cartItem.productId }
      product?.price?.times(cartItem.quantity).orZero()
    }
  }
}
