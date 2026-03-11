package com.nutrisport.data

import com.nutrisport.data.mapper.ProductDtoToDomainMapper
import com.nutrisport.data.mapper.ProductToDtoMapper
import com.nutrisport.shared.domain.AdminRepository
import com.nutrisport.shared.domain.PlatformFile
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AdminRepositoryImpl(
  private val productMapper: ProductMapper,
  private val dtoToDomain: ProductDtoToDomainMapper,
  private val domainToDto: ProductToDtoMapper,
) : AdminRepository {
  private val productCollection = Firebase.firestore.collection(collectionPath = COLLECTION_NAME)

  companion object {
    private const val COLLECTION_NAME = "product"
  }

  override fun getCurrentUserId(): String? = currentUserId()

  override suspend fun createNewProduct(product: Product): DomainResult<Unit> {
    return try {
      withAuth {
        val dto = domainToDto.map(
          product.copy(
            title = product.title.lowercase(),
            category = product.category.filter { it.isLetter() },
          ),
        )
        productCollection.document(product.id).set(dto)
        Either.Right(Unit)
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while creating a product: ${e.message}"))
    }
  }

  override suspend fun uploadImageToStorage(file: PlatformFile): DomainResult<String> {
    return try {
      withAuth {
        val path = Firebase.storage.reference.child("images/${Uuid.random().toHexString()}")
        val url = withTimeout(20000) {
          path.putFile(file.toStorageFile())
          path.getDownloadUrl()
        }
        Either.Right(url)
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error uploading image: ${e.message}"))
    }
  }

  override suspend fun deleteImageFromStorage(downloadUrl: String): DomainResult<Unit> {
    return try {
      val path = extractFirebaseStoragePath(downloadUrl)
      if (path != null) {
        Firebase.storage.reference(path).delete()
        Either.Right(Unit)
      } else {
        Either.Left(AppError.Unknown("Error while extracting the path"))
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while deleting the image: ${e.message}"))
    }
  }

  override fun readLastTenProducts(): Flow<DomainResult<List<Product>>> {
    return authenticatedProductDtoListFlow(
      productMapper,
      errorMessage = "Error while retrieving products",
    ) {
      productCollection.orderBy("createdAt", Direction.DESCENDING).limit(10)
    }.map { result ->
      result.map { dtos -> dtoToDomain.map(dtos) }
    }
  }

  override suspend fun readProductById(id: String): DomainResult<Product> {
    return try {
      if (currentUserId() == null) return Either.Left(AppError.Unauthorized())
      val document = productCollection.document(id).get()
      if (document.exists) {
        Either.Right(dtoToDomain.map(productMapper.map(document)))
      } else {
        Either.Left(AppError.NotFound("Product is not available"))
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while reading selected product: ${e.message}"))
    }
  }

  override suspend fun updateProductThumbnail(
    productId: String,
    downloadUrl: String,
  ): DomainResult<Unit> {
    return try {
      withAuth {
        val document = productCollection.document(productId).get()
        if (document.exists) {
          productCollection.document(productId).updateFields { "thumbnail" to downloadUrl }
          Either.Right(Unit)
        } else {
          Either.Left(AppError.NotFound("Product is not available"))
        }
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while updating image thumbnail: ${e.message}"))
    }
  }

  override suspend fun updateProduct(product: Product): DomainResult<Unit> {
    return try {
      withAuth {
        val document = productCollection.document(product.id).get()
        if (document.exists) {
          val dto = domainToDto.map(
            product.copy(
              title = product.title.lowercase(),
              category = product.category.filter { it.isLetter() },
            ),
          )
          productCollection.document(product.id).update(dto)
          Either.Right(Unit)
        } else {
          Either.Left(AppError.NotFound("Product is not available"))
        }
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while updating product: ${e.message}"))
    }
  }

  override suspend fun deleteProduct(productId: String): DomainResult<Unit> {
    return try {
      withAuth {
        val document = productCollection.document(productId).get()
        if (document.exists) {
          productCollection.document(productId).delete()
          Either.Right(Unit)
        } else {
          Either.Left(AppError.NotFound("Product is not available"))
        }
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while deleting the product: ${e.message}"))
    }
  }

  override fun searchProductByTitle(query: String): Flow<DomainResult<List<Product>>> {
    currentUserId() ?: return flowOf(Either.Left(AppError.Unauthorized()))
    val queryText = query.lowercase().trim()
    return productCollection
      .orderBy("title")
      .startAt(queryText)
      .endAt(queryText + "\uf8ff")
      .toProductDtoListFlow(productMapper, errorMessage = "Error while searching products")
      .map { result ->
        result.map { dtos -> dtoToDomain.map(dtos) }
      }
  }

  private fun extractFirebaseStoragePath(downloadUrl: String): String? {
    val startIndex = downloadUrl.indexOf("/o/") + 3
    if (startIndex < 3) return null
    val endIndex = downloadUrl.indexOf("?", startIndex)
    val encodedPath = if (endIndex != -1) {
      downloadUrl.substring(startIndex, endIndex)
    } else {
      downloadUrl.substring(startIndex)
    }
    return encodedPath.replace("%2F", "/").replace("%20", " ")
  }
}
