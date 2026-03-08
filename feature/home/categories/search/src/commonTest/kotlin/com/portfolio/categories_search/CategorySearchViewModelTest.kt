package com.portfolio.categories_search

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import androidx.lifecycle.SavedStateHandle
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
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
class CategorySearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun product(id: String, title: String) = Product(
        id = id, title = title, description = "desc",
        thumbnail = "url", category = "Protein", price = 10.0,
    )

    private fun fakeProductRepo(products: List<Product> = emptyList()) = object : ProductRepository {
        override fun readDiscountedProducts() = flowOf(Either.Right(emptyList<Product>()))
        override fun readNewProducts() = flowOf(Either.Right(emptyList<Product>()))
        override fun getCurrentUserId() = "user-1"
        override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> =
            flowOf(Either.Right(products.first { it.id == id }))
        override fun readProductsByIdsFlow(ids: List<String>) =
            flowOf(Either.Right(emptyList<Product>()))
        override fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>> =
            flowOf(Either.Right(products))
    }

    private fun createViewModel(products: List<Product> = emptyList()): CategorySearchViewModel {
        val savedState = SavedStateHandle(mapOf("category" to "Protein"))
        return CategorySearchViewModel(fakeProductRepo(products), savedState)
    }

    @Test
    fun `should show all products when search query is empty`() = runTest(testDispatcher) {
        val products = listOf(product("1", "WHEY"), product("2", "CASEIN"))
        val viewModel = createViewModel(products)

        viewModel.filteredProducts.test {
            assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<List<Product>>>()
            val data = (result as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!).hasSize(2)
        }
    }

    @Test
    fun `should update search query`() {
        val viewModel = createViewModel()

        viewModel.updateSearchQuery("whey")

        assertThat(viewModel.searchQuery.value).isEqualTo("whey")
    }
}
