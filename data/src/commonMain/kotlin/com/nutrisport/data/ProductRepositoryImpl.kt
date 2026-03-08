package com.nutrisport.data

import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ProductRepositoryImpl(
  private val mapper: ProductMapper = ProductMapper(),
) : ProductRepository {
  private val productCollection = Firebase.firestore.collection(collectionPath = "product")

  override fun getCurrentUserId(): String? = currentUserId()

  override fun readDiscountedProducts(): Flow<RequestState<List<Product>>> =
    authenticatedProductListFlow(mapper, errorMessage = "Error while retrieving products") {
      productCollection.where { "isDiscounted" equalTo true }
    }

  override fun readNewProducts(): Flow<RequestState<List<Product>>> =
    authenticatedProductListFlow(mapper, errorMessage = "Error while retrieving products") {
      productCollection.where { "isNew" equalTo true }
    }

  override fun readProductByIdFlow(id: String): Flow<RequestState<Product>> {
    if (currentUserId() == null) return flowOf(RequestState.Error("User is not available"))
    return productCollection.document(id).snapshots
      .map<_, RequestState<Product>> { document ->
        if (document.exists) {
          RequestState.Success(mapper.map(document))
        } else {
          RequestState.Error("Document $id doesn't exist")
        }
      }
      .onStart { emit(RequestState.Loading) }
      .catch { emit(RequestState.Error("Error while reading selected product: ${it.message}")) }
  }

  override fun readProductsByIdsFlow(ids: List<String>): Flow<RequestState<List<Product>>> {
    if (currentUserId() == null) return flowOf(RequestState.Error("User is not available"))
    if (ids.isEmpty()) return flowOf(RequestState.Success(emptyList()))

    val chunkFlows = ids.chunked(10).map { chunk ->
      productCollection
        .where { "id" inArray chunk }
        .snapshots
        .map { query -> query.documents.map { mapper.map(it) } }
    }

    return combine(chunkFlows) { arrays -> arrays.toList().flatten() }
      .map<_, RequestState<List<Product>>> { RequestState.Success(it) }
      .onStart { emit(RequestState.Loading) }
      .catch { emit(RequestState.Error("Error while reading selected products: ${it.message}")) }
  }

  override fun readProductsByCategoryFlow(category: ProductCategory): Flow<RequestState<List<Product>>> =
    authenticatedProductListFlow(mapper, errorMessage = "Error while reading products") {
      productCollection.where { "category" equalTo category.name }
    }
}
