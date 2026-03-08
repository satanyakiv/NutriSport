package com.nutrisport.products_overview

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.RequestState
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
            override fun readDiscountedProducts() = flowOf(RequestState.Success(discountedProducts))
            override fun readNewProducts() = flowOf(RequestState.Success(newProducts))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> = flowOf(RequestState.Loading)
            override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> = flowOf(RequestState.Loading)
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>> = flowOf(RequestState.Loading)
        }

        // Act
        val viewModel = ProductsOverviewViewModel(repo)

        // Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<RequestState.Loading>()
            val success = awaitItem()
            assertThat(success).isInstanceOf<RequestState.Success<List<Product>>>()
            assertThat((success as RequestState.Success).data).hasSize(2)
        }
    }

    @Test
    fun `should deduplicate products by id`() = runTest(testDispatcher) {
        // Arrange
        val sharedProduct = product("1", "SHARED", isNew = true, isDiscounted = true)

        val repo = object : ProductRepository {
            override fun readDiscountedProducts() = flowOf(RequestState.Success(listOf(sharedProduct)))
            override fun readNewProducts() = flowOf(RequestState.Success(listOf(sharedProduct)))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> = flowOf(RequestState.Loading)
            override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> = flowOf(RequestState.Loading)
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>> = flowOf(RequestState.Loading)
        }

        // Act
        val viewModel = ProductsOverviewViewModel(repo)

        // Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<RequestState.Loading>()
            val success = awaitItem() as RequestState.Success
            assertThat(success.data).hasSize(1)
            assertThat(success.data.first().id).isEqualTo("1")
        }
    }

    @Test
    fun `should propagate error from new products`() = runTest(testDispatcher) {
        // Arrange
        val repo = object : ProductRepository {
            override fun readDiscountedProducts() = flowOf(RequestState.Success(emptyList<Product>()))
            override fun readNewProducts(): Flow<RequestState<List<Product>>> = flowOf(RequestState.Error("Network error"))
            override fun getCurrentUserId() = "user-1"
            override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> = flowOf(RequestState.Loading)
            override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> = flowOf(RequestState.Loading)
            override fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>> = flowOf(RequestState.Loading)
        }

        // Act
        val viewModel = ProductsOverviewViewModel(repo)

        // Assert
        viewModel.products.test {
            assertThat(awaitItem()).isInstanceOf<RequestState.Loading>()
            val error = awaitItem()
            assertThat(error).isInstanceOf<RequestState.Error>()
        }
    }
}
