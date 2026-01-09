package com.nutrisport.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.data.domain.CustomerRepository
import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeGraphViewModel(
  private val customerRepository: CustomerRepository,
) : ViewModel() {

  val customer = customerRepository.readCustomerFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), RequestState.Loading)
  fun signOut(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      val result = withContext(Dispatchers.IO) {
        customerRepository.signOut()
      }
      if (result.isSuccess()) {
        onSuccess()
      } else {
        onError(result.getErrorMessage())
      }
    }
  }
}