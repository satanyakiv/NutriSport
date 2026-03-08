package com.nutrisport.data.domain

// AdminRepository stays in data layer because it depends on Firebase Storage File type.
// Moving to shared/domain would require abstracting File, which adds complexity without benefit.

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
  fun getCurrentUserId(): String?
  suspend fun createNewProduct(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  )
  suspend fun uploadImageToStorage(file: File): String?

  suspend fun deleteImageFromStorage(
    downloadUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  )

  fun readLastTenProducts(): Flow<RequestState<List<Product>>>

  suspend fun readProductById(id: String): RequestState<Product>

  suspend fun updateProductThumbnail(
    productId: String,
    downloadUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  )

  suspend fun updateProduct(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  )

  suspend fun deleteProduct(
    productId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  )

  fun searchProductByTitle(query: String): Flow<RequestState<List<Product>>>
}