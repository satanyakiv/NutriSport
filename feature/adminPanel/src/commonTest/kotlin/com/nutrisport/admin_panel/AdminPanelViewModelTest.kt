package com.nutrisport.admin_panel

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminPanelViewModelTest {

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
    fun `should show all products when search is empty`() = runTest(testDispatcher) {
        // Arrange
        val products = listOf(fakeProduct(id = "1", title = "WHEY"), fakeProduct(id = "2", title = "CREATINE"))
        val viewModel = AdminPanelViewModel(FakeAdminRepository(products))

        // Act & Assert
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
    fun `should search products by title`() = runTest(testDispatcher) {
        // Arrange
        val allProducts = listOf(fakeProduct(id = "1", title = "WHEY"), fakeProduct(id = "2", title = "CREATINE"))
        val searchResults = listOf(fakeProduct(id = "1", title = "WHEY"))
        val viewModel = AdminPanelViewModel(FakeAdminRepository(allProducts, searchResults))

        // Assert — subscribe first (WhileSubscribed requires active collector)
        viewModel.filteredProducts.test {
            skipItems(1) // skip initial Loading

            // Act — update query and advance past debounce
            viewModel.updateSearchQuery("whey")
            advanceTimeBy(1100)
            advanceUntilIdle()

            val result = awaitItem()
            assertThat(result).isInstanceOf<UiState.Content<List<Product>>>()
            val data = (result as UiState.Content).result.getOrNull()
            assertThat(data).isNotNull()
            assertThat(data!!).hasSize(1)
            assertThat(data.first().title).isEqualTo("WHEY")
        }
    }
}
