package com.nutrisport.auth.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

class AuthViewModel(
  private val customerRepository: CustomerRepository,
) : ViewModel() {
  fun createCustomer(
    user: FirebaseUser?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      customerRepository.createCustomer(
        uid = user?.uid ?: "",
        displayName = user?.displayName,
        email = user?.email,
      ).fold(
        ifLeft = { error -> onError(error.message) },
        ifRight = { onSuccess() }
      )
    }
  }
}
