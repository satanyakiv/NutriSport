package com.nutrisport.shared.domain

import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeProductRepository : ProductRepository {
    var fakeCurrentUserId: String? = "user-1"
    var discountedProducts: Flow<DomainResult<List<Product>>> =
        flowOf(Either.Right(emptyList()))
    var newProducts: Flow<DomainResult<List<Product>>> =
        flowOf(Either.Right(emptyList()))
    var productByIdFlows: MutableMap<String, Flow<DomainResult<Product>>> = mutableMapOf()
    var productsByIdsFlow: Flow<DomainResult<List<Product>>> =
        flowOf(Either.Right(emptyList()))
    var productsByCategoryFlow: Flow<DomainResult<List<Product>>> =
        flowOf(Either.Right(emptyList()))

    override fun readDiscountedProducts() = discountedProducts
    override fun readNewProducts() = newProducts
    override fun getCurrentUserId() = fakeCurrentUserId

    override fun readProductByIdFlow(id: String) =
        productByIdFlows[id] ?: flowOf(Either.Left(AppError.NotFound("Product not found")))

    override fun readProductsByIdsFlow(ids: List<String>) = productsByIdsFlow

    override fun readProductsByCategoryFlow(category: ProductCategory) = productsByCategoryFlow
}
