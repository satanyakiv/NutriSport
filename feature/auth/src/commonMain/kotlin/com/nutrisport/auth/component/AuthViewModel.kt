package com.nutrisport.auth.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import kotlinx.coroutines.launch

class AuthViewModel(
  private val customerRepository: CustomerRepository,
) : ViewModel() {
  fun createCustomer(
    uid: String,
    displayName: String?,
    email: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      customerRepository.createCustomer(
        uid = uid,
        displayName = displayName,
        email = email,
      ).fold(
        ifLeft = { error -> onError(error.message) },
        ifRight = { onSuccess() }
      )
    }
  }
}
