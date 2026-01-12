package com.nutrisport.data.domain

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
  fun  readDiscountedProducts(): Flow<RequestState<List<Product>>>
  fun  readNewProducts(): Flow<RequestState<List<Product>>>
  fun getCurrentUserId(): String?
  fun readProductByIdFlow(id: String): Flow<RequestState<Product>>
  fun readProductsByIdsFlow(toList: List<String>): Flow<RequestState<List<Product>>>
}