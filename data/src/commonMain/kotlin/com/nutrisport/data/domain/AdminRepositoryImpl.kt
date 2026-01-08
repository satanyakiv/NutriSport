package com.nutrisport.data.domain

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withTimeout
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AdminRepositoryImpl : AdminRepository {
  override fun getCurrentUserId(): String? = Firebase.auth.currentUser?.uid

  override suspend fun createNewProduct(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val currentUserId = getCurrentUserId()
      if (currentUserId != null) {
        val firestore = Firebase.firestore
        val productCollection = firestore.collection(collectionPath = "product")
        productCollection.document(product.id).set(product)
        onSuccess()
      } else {
        onError("User is not available")
      }
    } catch (e: Exception) {
      onError("Error while creating a product: ${e.message}")
    }
  }

  override suspend fun uploadImageToStorage(file: File): String? {
    return if (getCurrentUserId() == null) null else {
      val storage = Firebase.storage.reference
      val path = storage.child("images/${Uuid.random().toHexString()}")
      try {
        withTimeout(20000) {
          path.putFile(file)
          path.getDownloadUrl()
        }
      } catch (e: Exception) {
        null
      }
    }
  }

  override suspend fun deleteImageFromStorage(
    downloadUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
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

  override fun readLastTenProducts(): Flow<RequestState<List<Product>>> {
    getCurrentUserId()
      ?: return flowOf(RequestState.Error("User is not available"))
    return Firebase.firestore
      .collection(collectionPath = "product")
      .orderBy("createdAt", Direction.DESCENDING)
      .limit(10)
      .snapshots
      .map { query ->
        val products = query.documents.map { document ->
          Product(
            id = document.id,
            createdAt = document.get("createdAt"),
            title = document.get("title"),
            description = document.get("description"),
            thumbnail = document.get("thumbnail"),
            category = document.get("category"),
            flavors = document.get("flavors"),
            weight = document.get("weight"),
            price = document.get("price"),
            isPopular = document.get("isPopular"),
            isNew = document.get("isNew"),
          )
        }
        RequestState.Success(products) as RequestState<List<Product>>
      }
      .onStart { emit(RequestState.Loading) }
      .catch {
        emit(RequestState.Error("Error while retriving products: ${it.message}"))
      }
  }

  override suspend fun readProductById(id: String): RequestState<Product> {
    return try {
      val userId = getCurrentUserId()
      if (userId != null) {
        val database = Firebase.firestore
        val productDocument = database.collection(collectionPath = "product")
          .document(id)
          .get()
        if (productDocument.exists) {
          val product = Product(
            id = productDocument.id,
            createdAt = productDocument.get("createdAt"),
            title = productDocument.get("title"),
            description = productDocument.get("description"),
            thumbnail = productDocument.get("thumbnail"),
            category = productDocument.get("category"),
            flavors = productDocument.get("flavors"),
            weight = productDocument.get("weight"),
            price = productDocument.get("price"),
            isPopular = productDocument.get("isPopular"),
            isNew = productDocument.get("isNew"),
          )
          RequestState.Success(product)
        } else {
          RequestState.Error("Product is not available")
        }
      } else {
        RequestState.Error("User is not available")
      }
    } catch (e: Exception) {
      RequestState.Error("Error while reading selected product: ${e.message}")
    }
  }

  override suspend fun updateImageThumbnail(
    productId: String,
    downloadUrl: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val userId = getCurrentUserId()
      if (userId != null) {
        val productCollection = Firebase.firestore
          .collection("product")
        val document = productCollection.document(productId)
          .get()
        if (document.exists) {
          productCollection.document(productId)
            .updateFields { "thumbnail" to downloadUrl }
          onSuccess()
        } else {
          onError("Product is not available")
        }
      } else {
        onError("User is not available")
      }
    } catch (e: Exception) {
      onError("Error while updating image thumbnail: ${e.message}")
    }
  }

  override suspend fun updateProduct(
    product: Product,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val userId = getCurrentUserId()
      if (userId != null) {
        val productCollection = Firebase.firestore
          .collection("product")
        val document = productCollection.document(product.id)
          .get()
        if (document.exists) {
          productCollection.document(product.id)
            .update(product)
          onSuccess()
        } else {
          onError("Product is not available")
        }
      } else {
        onError("User is not available")
      }
    } catch (e: Exception) {
      onError("Error while updating product: ${e.message}")
    }
  }

  private fun extractFirebaseStoragePath(downloadUrl: String): String? {
    val startIndex = downloadUrl.indexOf("/o/") + 3 //3 character in /o/
    if (startIndex < 3) return null
    val endIndex = downloadUrl.indexOf("?", startIndex)
    val encodedPath = if (endIndex != -1) {
      downloadUrl.substring(startIndex, endIndex)
    } else {
      downloadUrl.substring(startIndex)
    }
    return decodeFirebasePath(encodedPath)
  }

  private fun decodeFirebasePath(path: String): String {
    return path
      .replace("%2F", "/")
      .replace("%20", " ")
  }
}
