package com.nutrisport.products_overview

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
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

    private fun product(id: String, title: String, isNew: Boolean = false, isDiscounted: Boolean = false) =
        Product(
            id = id, title = title, description = "desc",
            thumbnail = "url", category = "Protein", price = 10.0,
            isNew = isNew, isDiscounted = isDiscounted,
        )

    @Test
    fun `should combine new and discounted products`() = runTest(testDispatcher) {
        // Arrange
        val newProducts = listOf(product("1", "NEW", isNew = true))
        val discountedProducts = listOf(product("2", "DISCOUNTED", isDiscounted = true))

        val repo = object : ProductRepository {
            override fun readDiscountedProducts() = flowOf(Either.Right(discountedProducts))
            override fun readNewProducts() = flowOf(Either.Right(newProducts))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
                flowOf(Either.Right(product(id, "P")))
            override fun readProductsByIdsFlow(ids: List<String>): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
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
        val sharedProduct = product("1", "SHARED", isNew = true, isDiscounted = true)

        val repo = object : ProductRepository {
            override fun readDiscountedProducts() = flowOf(Either.Right(listOf(sharedProduct)))
            override fun readNewProducts() = flowOf(Either.Right(listOf(sharedProduct)))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
                flowOf(Either.Right(product(id, "P")))
            override fun readProductsByIdsFlow(ids: List<String>): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
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
        val repo = object : ProductRepository {
            override fun readDiscountedProducts() = flowOf(Either.Right(emptyList<Product>()))
            override fun readNewProducts(): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Left(AppError.Network("Network error")))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
                flowOf(Either.Right(product(id, "P")))
            override fun readProductsByIdsFlow(ids: List<String>): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
                flowOf(Either.Right(emptyList()))
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
