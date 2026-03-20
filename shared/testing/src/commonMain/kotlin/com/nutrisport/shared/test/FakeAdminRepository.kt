package com.nutrisport.shared.test

import com.nutrisport.shared.domain.AdminRepository
import com.nutrisport.shared.domain.PlatformFile
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAdminRepository(
  private val products: List<Product> = emptyList(),
  private val searchResults: List<Product> = emptyList(),
) : AdminRepository {
  override fun getCurrentUserId() = "user-1"
  override suspend fun createNewProduct(product: Product): DomainResult<Unit> =
    Either.Right(Unit)
  override suspend fun uploadImageToStorage(file: PlatformFile): DomainResult<String> =
    Either.Right("https://example.com/img.jpg")
  override suspend fun deleteImageFromStorage(downloadUrl: String): DomainResult<Unit> =
    Either.Right(Unit)
  override fun readLastTenProducts() = flowOf(Either.Right(products))
  override suspend fun readProductById(id: String): DomainResult<Product> =
    products.find { it.id == id }?.let { Either.Right(it) }
      ?: Either.Left(AppError.NotFound("Not found"))
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
