package com.nutrisport.checkout


import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Country
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.PhoneNumber
import com.nutrisport.shared.domain.usecase.CreateOrderUseCase
import com.nutrisport.shared.domain.usecase.UpdateCustomerUseCase
import com.nutrisport.shared.domain.usecase.ValidateProfileFormUseCase
import com.nutrisport.shared.util.RequestState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class CheckoutScreenState(
  val id: String = "",
  val firstName: String = "",
  val lastName: String = "",
  val email: String = "",
  val city: String? = null,
  val postalCode: Int? = null,
  val address: String? = null,
  val country: Country = Country.Serbia,
  val phoneNumber: PhoneNumber? = null,
  val cart: List<com.nutrisport.shared.domain.CartItem> = emptyList(),
)

class CheckoutViewModel(
  private val customerRepository: CustomerRepository,
  private val createOrderUseCase: CreateOrderUseCase,
  private val updateCustomerUseCase: UpdateCustomerUseCase,
  private val validateProfileFormUseCase: ValidateProfileFormUseCase,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  var screenReady: RequestState<Unit> by mutableStateOf(RequestState.Loading)
  var screenState: CheckoutScreenState by mutableStateOf(CheckoutScreenState())
    private set

  val isFormValid: Boolean
    get() = with(screenState) {
      validateProfileFormUseCase(
        firstName = firstName,
        lastName = lastName,
        city = city,
        postalCode = postalCode,
        address = address,
        phoneNumber = phoneNumber,
      )
    }

  init {
    viewModelScope.launch {
      customerRepository.readCustomerFlow().collectLatest { data ->
        if (data.isSuccess()) {
          val fetchedCustomer = data.getSuccessData()
          screenState = CheckoutScreenState(
            id = fetchedCustomer.id,
            firstName = fetchedCustomer.firstName,
            lastName = fetchedCustomer.lastName,
            email = fetchedCustomer.email,
            city = fetchedCustomer.city,
            postalCode = fetchedCustomer.postalCode,
            address = fetchedCustomer.address,
            phoneNumber = fetchedCustomer.phoneNumber,
            country = Country.entries.firstOrNull { it.dialCode == fetchedCustomer.phoneNumber?.dialCode }
              ?: Country.Serbia,
            cart = fetchedCustomer.cart
          )
          screenReady = RequestState.Success(Unit)
        } else if (data.isError()) {
          screenReady = RequestState.Error(data.getErrorMessage())
        }
      }
    }
  }

  fun updateFirstName(value: String) {
    screenState = screenState.copy(firstName = value)
  }

  fun updateLastName(value: String) {
    screenState = screenState.copy(lastName = value)
  }

  fun updateCity(value: String) {
    screenState = screenState.copy(city = value)
  }

  fun updatePostalCode(value: Int?) {
    screenState = screenState.copy(postalCode = value)
  }

  fun updateAddress(value: String) {
    screenState = screenState.copy(address = value)
  }

  fun updateCountry(value: Country) {
    screenState = screenState.copy(
      country = value,
      phoneNumber = screenState.phoneNumber?.copy(
        dialCode = value.dialCode
      )
    )
  }

  fun updatePhoneNumber(value: String) {
    screenState = screenState.copy(
      phoneNumber = PhoneNumber(
        dialCode = screenState.country.dialCode,
        number = value
      )
    )
  }

  fun payOnDelivery(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    updateCustomer(
      onSuccess = {
        createTheOrder(
          onSuccess = onSuccess,
          onError = onError
        )
      },
      onError = onError
    )
  }

  private fun updateCustomer(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      updateCustomerUseCase(
        customer = Customer(
          id = screenState.id,
          firstName = screenState.firstName,
          lastName = screenState.lastName,
          email = screenState.email,
          city = screenState.city,
          postalCode = screenState.postalCode,
          address = screenState.address,
          phoneNumber = screenState.phoneNumber
        ),
        onSuccess = onSuccess,
        onError = onError
      )
    }
  }

  private fun createTheOrder(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    Napier.d("Creating order for customer=${screenState.id} cartSize=${screenState.cart.size}")
    viewModelScope.launch {
      createOrderUseCase(
        customerId = screenState.id,
        cartItems = screenState.cart,
        totalAmount = savedStateHandle.get<Double>("totalAmount") ?: 0.0,
        onSuccess = onSuccess,
        onError = onError,
      )
    }
  }
}
