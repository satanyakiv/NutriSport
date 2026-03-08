package com.nutrisport.admin_panel

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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

    private fun product(id: String, title: String) = Product(
        id = id, title = title, description = "desc",
        thumbnail = "url", category = "Protein", price = 10.0,
    )

    private fun fakeAdminRepo(
        products: List<Product> = emptyList(),
        searchResults: List<Product> = emptyList(),
    ): AdminRepository = object : AdminRepository {
        override fun getCurrentUserId() = "user-1"
        override suspend fun createNewProduct(product: Product): DomainResult<Unit> =
            Either.Right(Unit)
        override suspend fun uploadImageToStorage(file: dev.gitlive.firebase.storage.File) = null
        override suspend fun deleteImageFromStorage(downloadUrl: String): DomainResult<Unit> =
            Either.Right(Unit)
        override fun readLastTenProducts() = flowOf(Either.Right(products))
        override suspend fun readProductById(id: String): DomainResult<Product> =
            Either.Right(products.first { it.id == id })
        override suspend fun updateProductThumbnail(
            productId: String,
            downloadUrl: String,
        ): DomainResult<Unit> = Either.Right(Unit)
        override suspend fun updateProduct(product: Product): DomainResult<Unit> =
            Either.Right(Unit)
        override suspend fun deleteProduct(productId: String): DomainResult<Unit> =
            Either.Right(Unit)
        override fun searchProductByTitle(query: String): Flow<DomainResult<List<Product>>> =
            flowOf(Either.Right(searchResults))
    }

    @Test
    fun `should show all products when search is empty`() = runTest(testDispatcher) {
        // Arrange
        val products = listOf(product("1", "WHEY"), product("2", "CREATINE"))
        val viewModel = AdminPanelViewModel(fakeAdminRepo(products))

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
        val allProducts = listOf(product("1", "WHEY"), product("2", "CREATINE"))
        val searchResults = listOf(product("1", "WHEY"))
        val viewModel = AdminPanelViewModel(fakeAdminRepo(allProducts, searchResults))

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
