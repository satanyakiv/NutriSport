package com.nutrisport.data

import com.nutrisport.data.domain.ProductRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ProductRepositoryImpl(

) : ProductRepository {
  override fun readDiscountedProducts(): Flow<RequestState<List<Product>>> {
    getCurrentUserId()
      ?: return flowOf(RequestState.Error("User is not available"))
    return Firebase.firestore
      .collection(collectionPath = "product")
      .where { "isDiscounted" equalTo true }
      .snapshots
      .map { query ->
        val products = query.documents.map { document ->
          Product(
            id = document.id,
            createdAt = document.get("createdAt"),
            title = (document.get("title") as String).uppercase(),
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
        RequestState.Success(products.map { it.copy(title = it.title.uppercase()) }) as RequestState<List<Product>>
      }
      .onStart { emit(RequestState.Loading) }
      .catch {
        emit(RequestState.Error("Error while retriving products: ${it.message}"))
      }
  }

  override fun readNewProducts(): Flow<RequestState<List<Product>>> {
    getCurrentUserId()
      ?: return flowOf(RequestState.Error("User is not available"))
    return Firebase.firestore
      .collection(collectionPath = "product")
      .where { "isNew" equalTo true }
      .snapshots
      .map { query ->
        val products = query.documents.map { document ->
          Product(
            id = document.id,
            createdAt = document.get("createdAt"),
            title = (document.get("title") as String).uppercase(),
            description = document.get("description"),
            thumbnail = document.get("thumbnail"),
            category = document.get("category"),
            flavors = document.get("flavors"),
            weight = document.get("weight"),
            price = document.get("price"),
            isPopular = document.get("isPopular"),
            isNew = document.get("isNew"),
            isDiscounted = document.get("isDiscounted"),
          )
        }
        RequestState.Success(products.map { it.copy(title = it.title.uppercase()) }) as RequestState<List<Product>>
      }
      .onStart { emit(RequestState.Loading) }
      .catch {
        emit(RequestState.Error("Error while retriving products: ${it.message}"))
      }
  }

  override fun getCurrentUserId(): String? = Firebase.auth.currentUser?.uid
  override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> {
    getCurrentUserId()
      ?: return flowOf(RequestState.Error("User is not available"))
    return Firebase.firestore
      .collection(collectionPath = "product")
      .document(id)
      .snapshots
      .map { document ->
        if (document.exists) {
          val product = Product(
            id = document.id,
            createdAt = document.get("createdAt"),
            title = (document.get("title") as String).uppercase(),
            description = document.get("description"),
            thumbnail = document.get("thumbnail"),
            category = document.get("category"),
            flavors = document.get("flavors"),
            weight = document.get("weight"),
            price = document.get("price"),
            isPopular = document.get("isPopular"),
            isNew = document.get("isNew"),
            isDiscounted = document.get("isDiscounted"),
          )
          RequestState.Success(product)
        } else {
          RequestState.Error("Document $id doesn't exist")
        }
      }
      .onStart { emit(RequestState.Loading) }
      .catch {
        emit(RequestState.Error("Error while reading selected product: ${it.message}"))
      }
  }
}