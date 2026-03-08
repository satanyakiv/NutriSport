package com.nutrisport.shared.domain

import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
  fun  readDiscountedProducts(): Flow<RequestState<List<Product>>>
  fun  readNewProducts(): Flow<RequestState<List<Product>>>
  fun getCurrentUserId(): String?
  fun readProductByIdFlow(id: String): Flow<RequestState<Product>>
  fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>>

  fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>>
}
