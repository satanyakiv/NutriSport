package com.nutrisport.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.Country
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.PhoneNumber
import com.nutrisport.shared.domain.usecase.UpdateCustomerUseCase
import com.nutrisport.shared.domain.usecase.ValidateProfileFormUseCase
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileScreenState(
  val id: String = "",
  val firstName: String = "",
  val lastName: String = "",
  val email: String = "",
  val city: String? = null,
  val postalCode: Int? = null,
  val address: String? = null,
  val country: Country = Country.Serbia,
  val phoneNumber: PhoneNumber? = null,
)

class ProfileViewModel(
  private val customerRepository: CustomerRepository,
  private val updateCustomerUseCase: UpdateCustomerUseCase,
  private val validateProfileFormUseCase: ValidateProfileFormUseCase,
) : ViewModel() {
  var screenReady: UiState<Unit> by mutableStateOf(UiState.Loading)
  var screenState: ProfileScreenState by mutableStateOf(ProfileScreenState())
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
        data.fold(
          ifLeft = { error ->
            screenReady = UiState.Content(Either.Left(AppError.Unknown(error.message)))
          },
          ifRight = { fetchedCustomer ->
            screenState = ProfileScreenState(
              id = fetchedCustomer.id,
              firstName = fetchedCustomer.firstName,
              lastName = fetchedCustomer.lastName,
              email = fetchedCustomer.email,
              city = fetchedCustomer.city,
              postalCode = fetchedCustomer.postalCode,
              address = fetchedCustomer.address,
              phoneNumber = fetchedCustomer.phoneNumber,
              country = Country.entries.firstOrNull { it.dialCode == fetchedCustomer.phoneNumber?.dialCode }
                ?: Country.Serbia
            )
            screenReady = UiState.Content(Either.Right(Unit))
          }
        )
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

  fun updateCustomer(
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
      ).fold(
        ifLeft = { error -> onError(error.message) },
        ifRight = { onSuccess() }
      )
    }
  }
}
