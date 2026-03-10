package com.nutrisport.auth.component

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private var createCustomerResult: DomainResult<Unit> = Either.Right(Unit)

    private val fakeCustomerRepo = object : CustomerRepository {
        override fun getCurrentUserId() = "user-1"
        override fun readCustomerFlow(): Flow<DomainResult<Customer>> =
            flowOf(Either.Right(fakeCustomer()))
        override suspend fun createCustomer(
            uid: String,
            displayName: String?,
            email: String?,
        ): DomainResult<Unit> = createCustomerResult
        override suspend fun updateCustomer(customer: Customer) = Either.Right(Unit)
        override suspend fun addItemToCart(cartItem: CartItem) = Either.Right(Unit)
        override suspend fun updateCartItemQuantity(id: String, quantity: Int) = Either.Right(Unit)
        override suspend fun deleteCartItem(id: String) = Either.Right(Unit)
        override suspend fun deleteAllCartItems() = Either.Right(Unit)
        override suspend fun signOut() = Either.Right(Unit)
    }

    private fun fakeCustomer() = Customer(
        id = "user-1",
        firstName = "John",
        lastName = "Doe",
        email = "john@example.com",
    )

    @Test
    fun `should call onSuccess when createCustomer succeeds`() = runTest(testDispatcher) {
        // Arrange
        createCustomerResult = Either.Right(Unit)
        val viewModel = AuthViewModel(fakeCustomerRepo)
        var successCalled = false

        // Act
        viewModel.createCustomer(
            uid = "uid-1",
            displayName = "John",
            email = "john@example.com",
            onSuccess = { successCalled = true },
            onError = {},
        )
        advanceUntilIdle()

        // Assert
        assertThat(successCalled).isTrue()
    }

    @Test
    fun `should call onError when createCustomer fails`() = runTest(testDispatcher) {
        // Arrange
        createCustomerResult = Either.Left(AppError.Network("Connection failed"))
        val viewModel = AuthViewModel(fakeCustomerRepo)
        var errorMessage: String? = null

        // Act
        viewModel.createCustomer(
            uid = "uid-1",
            displayName = "John",
            email = "john@example.com",
            onSuccess = {},
            onError = { errorMessage = it },
        )
        advanceUntilIdle()

        // Assert
        assertThat(errorMessage).isEqualTo("Connection failed")
    }
}
