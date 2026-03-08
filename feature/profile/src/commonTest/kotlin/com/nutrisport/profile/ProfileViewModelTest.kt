package com.nutrisport.profile

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.PhoneNumber
import com.nutrisport.shared.domain.usecase.UpdateCustomerUseCase
import com.nutrisport.shared.domain.usecase.ValidateProfileFormUseCase
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val customerFlow = MutableStateFlow<DomainResult<Customer>>(
        Either.Right(Customer(id = "", firstName = "", lastName = "", email = "")),
    )
    private var updateCustomerError: String? = null

    private val fakeCustomerRepo = object : CustomerRepository {
        override fun getCurrentUserId() = "user-1"
        override fun readCustomerFlow(): Flow<DomainResult<Customer>> = customerFlow
        override suspend fun createCustomer(
            uid: String,
            displayName: String?,
            email: String?,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun updateCustomer(customer: Customer): DomainResult<Unit> {
            return updateCustomerError?.let { Either.Left(AppError.Unknown(it)) }
                ?: Either.Right(Unit)
        }
        override suspend fun addItemToCart(cartItem: CartItem): DomainResult<Unit> =
            Either.Right(Unit)
        override suspend fun updateCartItemQuantity(
            id: String,
            quantity: Int,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun deleteCartItem(id: String): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun deleteAllCartItems(): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun signOut(): DomainResult<Unit> = Either.Right(Unit)
    }

    private fun createViewModel(): ProfileViewModel {
        return ProfileViewModel(
            customerRepository = fakeCustomerRepo,
            updateCustomerUseCase = UpdateCustomerUseCase(fakeCustomerRepo),
            validateProfileFormUseCase = ValidateProfileFormUseCase(),
        )
    }

    @Test
    fun `should populate screen state when customer loads`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        customerFlow.value = Either.Right(
            Customer(
                id = "user-1", firstName = "John", lastName = "Doe",
                email = "john@e.com", city = "Belgrade", postalCode = 11000,
                address = "Main St 1", phoneNumber = PhoneNumber(381, "123456"),
            ),
        )
        advanceUntilIdle()

        assertThat(viewModel.screenState.firstName).isEqualTo("John")
        assertThat(viewModel.screenState.city).isEqualTo("Belgrade")
        assertThat(viewModel.screenReady).isInstanceOf<UiState.Content<Unit>>()
        val content = viewModel.screenReady as UiState.Content<Unit>
        assertThat(content.result).isInstanceOf<Either.Right<Unit>>()
    }

    @Test
    fun `should set error when customer load fails`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        customerFlow.value = Either.Left(AppError.Network("Network error"))
        advanceUntilIdle()

        assertThat(viewModel.screenReady).isInstanceOf<UiState.Content<Unit>>()
        val content = viewModel.screenReady as UiState.Content<Unit>
        assertThat(content.result).isInstanceOf<Either.Left<AppError>>()
    }

    @Test
    fun `should update first name`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.updateFirstName("Jane")

        assertThat(viewModel.screenState.firstName).isEqualTo("Jane")
    }

    @Test
    fun `should update last name`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.updateLastName("Smith")

        assertThat(viewModel.screenState.lastName).isEqualTo("Smith")
    }

    @Test
    fun `should update city`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.updateCity("Novi Sad")

        assertThat(viewModel.screenState.city).isEqualTo("Novi Sad")
    }

    @Test
    fun `should validate form as invalid when required fields empty`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        assertThat(viewModel.isFormValid).isEqualTo(false)
    }

    @Test
    fun `should validate form as valid when all fields filled`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        customerFlow.value = Either.Right(
            Customer(
                id = "user-1", firstName = "John", lastName = "Doe",
                email = "john@e.com", city = "Belgrade", postalCode = 11000,
                address = "Main St 1", phoneNumber = PhoneNumber(381, "123456"),
            ),
        )
        advanceUntilIdle()

        assertThat(viewModel.isFormValid).isEqualTo(true)
    }

    @Test
    fun `should call onSuccess when updating customer`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.updateCustomer(onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when updating customer fails`() = runTest(testDispatcher) {
        updateCustomerError = "Update failed"
        val viewModel = createViewModel()
        var errorMessage: String? = null

        viewModel.updateCustomer(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Update failed")
    }
}
