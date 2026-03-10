package com.portfolio.categories_search

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import androidx.lifecycle.SavedStateHandle
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.test.fakeProduct
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

    private fun createViewModel(products: List<Product> = emptyList()): CategorySearchViewModel {
        val repo = FakeProductRepository().apply {
            productsByCategoryFlow = flowOf(Either.Right(products))
        }
        val savedState = SavedStateHandle(mapOf("category" to "Protein"))
        return CategorySearchViewModel(repo, savedState)
    }

    @Test
    fun `should show all products when search query is empty`() = runTest(testDispatcher) {
        val products = listOf(fakeProduct(id = "1", title = "WHEY"), fakeProduct(id = "2", title = "CASEIN"))
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
