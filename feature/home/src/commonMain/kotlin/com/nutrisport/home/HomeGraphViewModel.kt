package com.nutrisport.home


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.usecase.CalculateCartTotalUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.domain.usecase.SignOutUseCase
import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeGraphViewModel(
  private val customerRepository: CustomerRepository,
  private val observeEnrichedCartUseCase: ObserveEnrichedCartUseCase,
  private val calculateCartTotalUseCase: CalculateCartTotalUseCase,
  private val signOutUseCase: SignOutUseCase,
) : ViewModel() {
  val customer = customerRepository.readCustomerFlow()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = RequestState.Loading
    )

  val cartItemsWithProducts = observeEnrichedCartUseCase()

  @OptIn(ExperimentalCoroutinesApi::class)
  val totalAmountFlow = cartItemsWithProducts
    .flatMapLatest { data ->
      if (data.isSuccess()) {
        val items = data.getSuccessData()
        val cartItems = items.map { it.first }
        val products = items.map { it.second }
        flowOf(RequestState.Success(calculateCartTotalUseCase(cartItems, products)))
      } else if (data.isError()) flowOf(RequestState.Error(data.getErrorMessage()))
      else flowOf(RequestState.Loading)
    }

  fun signOut(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      val result = withContext(Dispatchers.IO) {
        signOutUseCase()
      }
      if (result.isSuccess()) {
        onSuccess()
      } else if (result.isError()) {
        onError(result.getErrorMessage())
      }
    }
  }
}
