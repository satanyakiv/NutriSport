package com.nutrisport.data.domain

import com.nutrisport.shared.domain.Product
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.storage
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


  private fun extractFirebaseStoragePath(downloadUrl: String): String? {
    val startIndex = downloadUrl.indexOf("/o/") + 3 //3 character in /o/
    if (startIndex < 3) return null
    val endIndex = downloadUrl.indexOf("?", startIndex)
    val encodedPath = if(endIndex != -1) {
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
