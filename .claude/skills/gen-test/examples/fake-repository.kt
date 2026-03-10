// Example: Fake Repository with MutableSharedFlow for ViewModel testing
package com.nutrisport.shared.domain

import com.nutrisport.shared.domain.model.Product
import com.nutrisport.shared.domain.repository.ProductRepository
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeProductRepository : ProductRepository {

    // Flow-based methods: use MutableSharedFlow for full control
    private val _productsFlow = MutableSharedFlow<DomainResult<List<Product>>>()
    override fun readProductsFlow(): Flow<DomainResult<List<Product>>> = _productsFlow

    private val _productByIdFlow = MutableSharedFlow<DomainResult<Product>>()
    override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> = _productByIdFlow

    // One-shot methods: use mutable result properties
    var updateResult: DomainResult<Unit> = Either.Right(Unit)
    override suspend fun updateProduct(product: Product): DomainResult<Unit> = updateResult

    // Helpers: emit values in tests
    suspend fun emitProducts(result: DomainResult<List<Product>>) {
        _productsFlow.emit(result)
    }

    suspend fun emitProduct(result: DomainResult<Product>) {
        _productByIdFlow.emit(result)
    }
}

// Usage in ViewModel test:
//
// private val fakeRepo = FakeProductRepository()
// private val viewModel = ProductListViewModel(fakeRepo)
//
// @Test
// fun `should show products`() = runTest(testDispatcher) {
//     viewModel.products.test {
//         assertThat(awaitItem()).isInstanceOf<UiState.Loading>()
//         fakeRepo.emitProducts(Either.Right(listOf(fakeProduct())))
//         val content = awaitItem() as UiState.Content
//         assertThat(content.result.getOrNull()).hasSize(1)
//     }
// }
