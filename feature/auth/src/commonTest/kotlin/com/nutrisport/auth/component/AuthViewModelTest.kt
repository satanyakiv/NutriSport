package com.nutrisport.auth.component

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.nutrisport.shared.domain.navigation.NavigationCommand
import com.nutrisport.shared.navigation.Screen
import com.nutrisport.shared.test.FakeCustomerRepository
import com.nutrisport.shared.test.FakePendingDeeplinkStorage
import com.nutrisport.shared.test.FakeRouter
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
    private val fakeRouter = FakeRouter()
    private val fakePending = FakePendingDeeplinkStorage()

    @Test
    fun `should emit Replace HomeGraph when goToHome is called`() = runTest(testDispatcher) {
        val viewModel = AuthViewModel(fakeCustomerRepo, fakePending, fakeRouter)

        viewModel.goToHome()

        assertThat(fakeRouter.recordedCommands)
            .containsExactly(NavigationCommand.Replace(Screen.HomeGraph))
    }

    @Test
    fun `should resume parked deeplink when goToHome is called`() = runTest(testDispatcher) {
        fakePending.set(Screen.Profile)
        val viewModel = AuthViewModel(fakeCustomerRepo, fakePending, fakeRouter)

        viewModel.goToHome()

        assertThat(fakeRouter.recordedCommands)
            .containsExactly(NavigationCommand.Replace(Screen.Profile))
    }

    @Test
    fun `should call onSuccess when createCustomer succeeds`() = runTest(testDispatcher) {
        // Arrange
        val viewModel = AuthViewModel(fakeCustomerRepo, fakePending, fakeRouter)
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
        val viewModel = AuthViewModel(fakeCustomerRepo, fakePending, fakeRouter)
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
