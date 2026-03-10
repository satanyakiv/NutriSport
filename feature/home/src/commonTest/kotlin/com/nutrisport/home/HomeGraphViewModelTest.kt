package com.nutrisport.home

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.usecase.CalculateCartTotalUseCase
import com.nutrisport.shared.domain.usecase.EnrichCartWithProductsUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.domain.usecase.SignOutUseCase
import com.nutrisport.shared.test.FakeCustomerRepository
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
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
class HomeGraphViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        customerRepo: FakeCustomerRepository = FakeCustomerRepository(),
        productRepo: FakeProductRepository = FakeProductRepository(),
    ): HomeGraphViewModel {
        val enrichUseCase = EnrichCartWithProductsUseCase()
        val observeUseCase = ObserveEnrichedCartUseCase(customerRepo, productRepo, enrichUseCase)
        val calculateUseCase = CalculateCartTotalUseCase()
        val signOutUseCase = SignOutUseCase(customerRepo)
        return HomeGraphViewModel(customerRepo, observeUseCase, calculateUseCase, signOutUseCase)
    }

    @Test
    fun `should emit customer state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.customer.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<Customer>>()
            val data = (result as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!.firstName).isEqualTo("John")
        }
    }

    @Test
    fun `should emit error when customer fails`() = runTest(testDispatcher) {
        val repo = FakeCustomerRepository().apply {
            customerFlow.value = Either.Left(AppError.Network("Network error"))
        }
        val viewModel = createViewModel(customerRepo = repo)

        viewModel.customer.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<Customer>>()
            val content = result as UiState.Content<Customer>
            assertThat(content.result).isInstanceOf<Either.Left<AppError>>()
        }
    }

    @Test
    fun `should call onSuccess when sign out succeeds`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        var successCalled = false

        viewModel.signOut(onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()

        assertThat(successCalled).isEqualTo(true)
    }

    @Test
    fun `should call onError when sign out fails`() = runTest(testDispatcher) {
        val repo = FakeCustomerRepository().apply {
            signOutResult = Either.Left(AppError.Unknown("Sign out failed"))
        }
        val viewModel = createViewModel(customerRepo = repo)
        var errorMessage: String? = null

        viewModel.signOut(onSuccess = {}, onError = { errorMessage = it })
        advanceUntilIdle()

        assertThat(errorMessage).isEqualTo("Sign out failed")
    }
}
