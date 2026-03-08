package com.nutrisport.shared.domain

import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeProductRepository : ProductRepository {
    var currentUserId: String? = "user-1"
    var discountedProducts: Flow<RequestState<List<Product>>> =
        flowOf(RequestState.Success(emptyList()))
    var newProducts: Flow<RequestState<List<Product>>> =
        flowOf(RequestState.Success(emptyList()))
    var productByIdFlows: MutableMap<String, Flow<RequestState<Product>>> = mutableMapOf()
    var productsByIdsFlow: Flow<RequestState<List<Product>>> =
        flowOf(RequestState.Success(emptyList()))
    var productsByCategoryFlow: Flow<RequestState<List<Product>>> =
        flowOf(RequestState.Success(emptyList()))

    override fun readDiscountedProducts() = discountedProducts
    override fun readNewProducts() = newProducts
    override fun getCurrentUserId() = currentUserId

    override fun readProductByIdFlow(id: String) =
        productByIdFlows[id] ?: flowOf(RequestState.Error("Product not found"))

    override fun readProductsByIdsFlow(ids: List<String>) = productsByIdsFlow

    override fun readProductsByCategoryFlow(category: ProductCategory) = productsByCategoryFlow
}
