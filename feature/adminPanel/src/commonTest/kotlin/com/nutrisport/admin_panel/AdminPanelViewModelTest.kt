package com.nutrisport.admin_panel

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
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

    private fun product(id: String, title: String) = Product(
        id = id, title = title, description = "desc",
        thumbnail = "url", category = "Protein", price = 10.0,
    )

    private fun fakeAdminRepo(
        products: List<Product> = emptyList(),
        searchResults: List<Product> = emptyList(),
    ): AdminRepository = object : AdminRepository {
        override fun getCurrentUserId() = "user-1"
        override suspend fun createNewProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {}
        override suspend fun uploadImageToStorage(file: dev.gitlive.firebase.storage.File) = null
        override suspend fun deleteImageFromStorage(downloadUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {}
        override fun readLastTenProducts() = flowOf(RequestState.Success(products))
        override suspend fun readProductById(id: String) = RequestState.Success(products.first { it.id == id })
        override suspend fun updateProductThumbnail(productId: String, downloadUrl: String, onSuccess: () -> Unit, onError: (String) -> Unit) {}
        override suspend fun updateProduct(product: Product, onSuccess: () -> Unit, onError: (String) -> Unit) {}
        override suspend fun deleteProduct(productId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {}
        override fun searchProductByTitle(query: String): Flow<RequestState<List<Product>>> =
            flowOf(RequestState.Success(searchResults))
    }

    @Test
    fun `should show all products when search is empty`() = runTest(testDispatcher) {
        // Arrange
        val products = listOf(product("1", "WHEY"), product("2", "CREATINE"))
        val viewModel = AdminPanelViewModel(fakeAdminRepo(products))

        // Act & Assert
        viewModel.filteredProducts.test {
            assertThat(awaitItem()).isInstanceOf<RequestState.Loading>()
            val result = awaitItem()
            assertThat(result).isInstanceOf<RequestState.Success<List<Product>>>()
            assertThat((result as RequestState.Success).data).hasSize(2)
        }
    }

    @Test
    fun `should search products by title`() = runTest(testDispatcher) {
        // Arrange
        val allProducts = listOf(product("1", "WHEY"), product("2", "CREATINE"))
        val searchResults = listOf(product("1", "WHEY"))
        val viewModel = AdminPanelViewModel(fakeAdminRepo(allProducts, searchResults))

        // Act
        viewModel.updateSearchQuery("whey")
        advanceUntilIdle()

        // Assert
        viewModel.filteredProducts.test {
            val result = awaitItem()
            assertThat(result).isInstanceOf<RequestState.Success<List<Product>>>()
            assertThat((result as RequestState.Success).data).hasSize(1)
            assertThat(result.data.first().title).isEqualTo("WHEY")
        }
    }
}
