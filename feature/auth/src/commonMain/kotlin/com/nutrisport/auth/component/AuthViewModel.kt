package com.nutrisport.auth.component

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.deeplink.PendingDeeplinkStorage
import com.nutrisport.shared.domain.navigation.Router
import com.nutrisport.shared.navigation.Screen
import kotlinx.coroutines.launch

class AuthViewModel(
  private val customerRepository: CustomerRepository,
  private val pendingDeeplinkStorage: PendingDeeplinkStorage,
  private val router: Router,
) : ViewModel() {
  // After sign-in, resume a deeplink parked while signed out; otherwise go Home.
  fun goToHome() = router.replaceWith(pendingDeeplinkStorage.take() ?: Screen.HomeGraph)

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
