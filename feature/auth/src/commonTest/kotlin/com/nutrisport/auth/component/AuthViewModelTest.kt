package com.nutrisport.auth.component

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.test.FakeCustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    private val fakeCustomerRepo = FakeCustomerRepository()

    @Test
    fun `should call onSuccess when createCustomer succeeds`() = runTest(testDispatcher) {
        // Arrange
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
        fakeCustomerRepo.createCustomerError = "Connection failed"
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
