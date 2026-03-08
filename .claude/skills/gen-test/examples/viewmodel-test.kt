// Example: ViewModel test with Turbine, TestDispatcher, fake repos
package com.nutrisport.details

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Fake data factory
    private fun fakeProduct(id: String = "prod-1") = Product(
        id = id, title = "WHEY PROTEIN", description = "desc",
        thumbnail = "url", category = "Protein", price = 29.99,
    )

    // Inline fake repository (or use FakeXxxRepository class)
    private val fakeProductRepo = object : ProductRepository {
        override fun readProductByIdFlow(id: String) =
            flowOf(Either.Right(fakeProduct(id)))
        // ... other methods
    }

    @Test
    fun `should load product by id`() = runTest(testDispatcher) {
        // Arrange
        val viewModel = createViewModel("prod-1")

        // Act & Assert (Turbine)
        viewModel.product.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val content = awaitItem() as UiState.Content
            assertThat(content.result.getOrNull()?.title).isEqualTo("WHEY PROTEIN")
        }
    }

    @Test
    fun `should call onError when operation fails`() = runTest(testDispatcher) {
        // Arrange
        val viewModel = createViewModel()
        var errorMessage: String? = null

        // Act
        viewModel.doSomething(onError = { errorMessage = it })
        advanceUntilIdle()

        // Assert
        assertThat(errorMessage).isNotNull()
    }
}
