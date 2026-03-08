package com.nutrisport.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.usecase.CalculateCartTotalUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.domain.usecase.SignOutUseCase
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeGraphViewModel(
  private val customerRepository: CustomerRepository,
  private val observeEnrichedCartUseCase: ObserveEnrichedCartUseCase,
  private val calculateCartTotalUseCase: CalculateCartTotalUseCase,
  private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
  val customer = customerRepository.readCustomerFlow()
    .map { UiState.Content(it) }
    .onStart<UiState<com.nutrisport.shared.domain.Customer>> { emit(UiState.Loading) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading
    )

  val cartItemsWithProducts = observeEnrichedCartUseCase()

  @OptIn(ExperimentalCoroutinesApi::class)
  val totalAmountFlow = cartItemsWithProducts
    .flatMapLatest { result ->
      result.fold(
        ifLeft = { error ->
          flowOf(UiState.Content(Either.Left(error)))
        },
        ifRight = { items ->
          val cartItems = items.map { it.first }
          val products = items.map { it.second }
          flowOf(UiState.Content(Either.Right(calculateCartTotalUseCase(cartItems, products))))
        }
      )
    }

  fun signOut(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      val result = signOutUseCase()
      result.fold(
        ifLeft = { error -> onError(error.message) },
        ifRight = { onSuccess() }
      )
    }
  }
}
