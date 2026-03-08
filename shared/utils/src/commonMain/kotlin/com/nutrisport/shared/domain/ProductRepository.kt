package com.nutrisport.shared.domain

import com.nutrisport.shared.util.DomainResult
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun readDiscountedProducts(): Flow<DomainResult<List<Product>>>
    fun readNewProducts(): Flow<DomainResult<List<Product>>>
    fun getCurrentUserId(): String?
    fun readProductByIdFlow(id: String): Flow<DomainResult<Product>>
    fun readProductsByIdsFlow(ids: List<String>): Flow<DomainResult<List<Product>>>
    fun readProductsByCategoryFlow(category: ProductCategory): Flow<DomainResult<List<Product>>>
}
