package com.nutrisport.data.domain

// AdminRepository stays in data layer because it depends on Firebase Storage File type.
// Moving to shared/domain would require abstracting File, which adds complexity without benefit.

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.DomainResult
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    fun getCurrentUserId(): String?
    suspend fun createNewProduct(product: Product): DomainResult<Unit>
    suspend fun uploadImageToStorage(file: File): String?
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
