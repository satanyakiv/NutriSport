package com.nutrisport.products_overview

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsOverviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should combine new and discounted products`() = runTest(testDispatcher) {
        // Arrange
        val repo = FakeProductRepository().apply {
            newProducts = flowOf(Either.Right(listOf(fakeProduct(id = "1", title = "NEW", isNew = true))))
            discountedProducts = flowOf(Either.Right(listOf(fakeProduct(id = "2", title = "DISCOUNTED", isDiscounted = true))))
        }

        // Act
        val viewModel = ProductsOverviewViewModel(repo)

        // Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val success = awaitItem()
            assertThat(success).isInstanceOf<UiState.Content<List<Product>>>()
            val data = (success as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!).hasSize(2)
        }
    }

    @Test
    fun `should deduplicate products by id`() = runTest(testDispatcher) {
        // Arrange
        val sharedProduct = fakeProduct(id = "1", title = "SHARED", isNew = true, isDiscounted = true)
        val repo = FakeProductRepository().apply {
            discountedProducts = flowOf(Either.Right(listOf(sharedProduct)))
            newProducts = flowOf(Either.Right(listOf(sharedProduct)))
        }

        // Act
        val viewModel = ProductsOverviewViewModel(repo)

        // Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val success = awaitItem()
            val data = (success as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!).hasSize(1)
            assertThat(data.first().id).isEqualTo("1")
        }
    }

    @Test
    fun `should propagate error from new products`() = runTest(testDispatcher) {
        // Arrange
        val repo = FakeProductRepository().apply {
            newProducts = flowOf(Either.Left(AppError.Network("Network error")))
        }

        // Act
        val viewModel = ProductsOverviewViewModel(repo)

        // Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val error = awaitItem()
            assertThat(error).isInstanceOf<UiState.Content<List<Product>>>()
            val content = error as UiState.Content<List<Product>>
            assertThat(content.result).isInstanceOf<Either.Left<AppError>>()
        }
    }
}
