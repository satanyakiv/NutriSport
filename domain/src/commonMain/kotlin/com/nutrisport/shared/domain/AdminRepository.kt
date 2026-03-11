package com.nutrisport.shared.domain

import com.nutrisport.shared.util.DomainResult
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
  fun getCurrentUserId(): String?
  suspend fun createNewProduct(product: Product): DomainResult<Unit>
  suspend fun uploadImageToStorage(file: PlatformFile): DomainResult<String>
  suspend fun deleteImageFromStorage(downloadUrl: String): DomainResult<Unit>
  fun readLastTenProducts(): Flow<DomainResult<List<Product>>>
  suspend fun readProductById(id: String): DomainResult<Product>
  suspend fun updateProductThumbnail(
    productId: String,
    downloadUrl: String,
  ): DomainResult<Unit>
  suspend fun updateProduct(product: Product): DomainResult<Unit>
  suspend fun deleteProduct(productId: String): DomainResult<Unit>
  fun searchProductByTitle(query: String): Flow<DomainResult<List<Product>>>
}
