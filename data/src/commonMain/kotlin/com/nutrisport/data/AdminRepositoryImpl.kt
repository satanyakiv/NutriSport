package com.nutrisport.data

import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withTimeout
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AdminRepositoryImpl : AdminRepository {
  private val productCollection = Firebase.firestore.collection(collectionPath = "product")

  override fun getCurrentUserId(): String? = currentUserId()

  override suspend fun createNewProduct(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      withAuth(onError) {
        productCollection.document(product.id).set(
          product.copy(
            title = product.title.lowercase(),
            category = product.category.filter { it.isLetter() },
          ),
        )
        onSuccess()
      }
    } catch (e: Exception) {
      onError("Error while creating a product: ${e.message}")
    }
  }

  override suspend fun uploadImageToStorage(file: File): String? {
    if (currentUserId() == null) return null
    val path = Firebase.storage.reference.child("images/${Uuid.random().toHexString()}")
    return try {
      withTimeout(20000) {
        path.putFile(file)
        path.getDownloadUrl()
      }
    } catch (e: Exception) {
      null
    }
  }

  override suspend fun deleteImageFromStorage(
    downloadUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      extractFirebaseStoragePath(downloadUrl)?.let {
        Firebase.storage.reference(it).delete()
        onSuccess()
      } ?: onError("Error while extracting the path")
    } catch (e: Exception) {
      onError("Error while deleting the image: ${e.message}")
    }
  }

  override fun readLastTenProducts(): Flow<RequestState<List<Product>>> =
    authenticatedProductListFlow(errorMessage = "Error while retrieving products") {
      productCollection.orderBy("createdAt", Direction.DESCENDING).limit(10)
    }

  override suspend fun readProductById(id: String): RequestState<Product> {
    return try {
      if (currentUserId() == null) return RequestState.Error("User is not available")
      val document = productCollection.document(id).get()
      if (document.exists) {
        RequestState.Success(document.toProduct())
      } else {
        RequestState.Error("Product is not available")
      }
    } catch (e: Exception) {
      RequestState.Error("Error while reading selected product: ${e.message}")
    }
  }

  override suspend fun updateProductThumbnail(
    productId: String,
    downloadUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      withAuth(onError) {
        val document = productCollection.document(productId).get()
        if (document.exists) {
          productCollection.document(productId).updateFields { "thumbnail" to downloadUrl }
          onSuccess()
        } else {
          onError("Product is not available")
        }
      }
    } catch (e: Exception) {
      onError("Error while updating image thumbnail: ${e.message}")
    }
  }

  override suspend fun updateProduct(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      withAuth(onError) {
        val document = productCollection.document(product.id).get()
        if (document.exists) {
          productCollection.document(product.id).update(
            product.copy(
              title = product.title.lowercase(),
              category = product.category.filter { it.isLetter() },
            ),
          )
          onSuccess()
        } else {
          onError("Product is not available")
        }
      }
    } catch (e: Exception) {
      onError("Error while updating product: ${e.message}")
    }
  }

  override suspend fun deleteProduct(
    productId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      withAuth(onError) {
        val document = productCollection.document(productId).get()
        if (document.exists) {
          productCollection.document(productId).delete()
          onSuccess()
        } else {
          onError("Product is not available")
        }
      }
    } catch (e: Exception) {
      onError("Error while deleting the product: ${e.message}")
    }
  }

  override fun searchProductByTitle(query: String): Flow<RequestState<List<Product>>> {
    currentUserId() ?: return flowOf(RequestState.Error("User is not available"))
    val queryText = query.lowercase().trim()
    return productCollection
      .orderBy("title")
      .startAt(queryText)
      .endAt(queryText + "\uf8ff")
      .toProductListFlow(errorMessage = "Error while searching products")
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
