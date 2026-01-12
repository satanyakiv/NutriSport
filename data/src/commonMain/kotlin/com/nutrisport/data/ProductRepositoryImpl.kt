package com.nutrisport.data

import com.nutrisport.data.domain.ProductRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ProductRepositoryImpl : ProductRepository {
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

  override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> =
    channelFlow {
      try {
        val userId = getCurrentUserId()
        if (userId != null) {
          val database = Firebase.firestore
          val productCollection = database.collection(collectionPath = "product")

          val allProducts = mutableListOf<Product>()
          val chunks = ids.chunked(10)

          chunks.forEachIndexed { index, chunk ->
            productCollection
              .where { "id" inArray chunk }
              .snapshots
              .collectLatest { query ->
                val products = query.documents.map { document ->
                  Product(
                    id = document.id,
                    title = document.get(field = "title"),
                    createdAt = document.get(field = "createdAt"),
                    description = document.get(field = "description"),
                    thumbnail = document.get(field = "thumbnail"),
                    category = document.get(field = "category"),
                    flavors = document.get(field = "flavors"),
                    weight = document.get(field = "weight"),
                    price = document.get(field = "price"),
                    isPopular = document.get(field = "isPopular"),
                    isDiscounted = document.get(field = "isDiscounted"),
                    isNew = document.get(field = "isNew")
                  )
                }
                allProducts.addAll(products.map { it.copy(title = it.title.uppercase()) })

                if (index == chunks.lastIndex) {
                  send(RequestState.Success(allProducts))
                }
              }
          }
        } else {
          send(RequestState.Error("User is not available."))
        }
      } catch (e: Exception) {
        send(RequestState.Error("Error while reading a selected product: ${e.message}"))
      }
    }
}