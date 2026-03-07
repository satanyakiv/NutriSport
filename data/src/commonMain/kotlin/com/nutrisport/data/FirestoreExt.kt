package com.nutrisport.data

import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun DocumentSnapshot.toProduct(): Product = Product(
  id = id,
  createdAt = get("createdAt"),
  title = (get("title") as String).uppercase(),
  description = get("description"),
  thumbnail = get("thumbnail"),
  category = get("category"),
  flavors = get("flavors"),
  weight = get("weight"),
  price = get("price"),
  isPopular = get("isPopular"),
  isDiscounted = get("isDiscounted"),
  isNew = get("isNew"),
)

internal fun currentUserId(): String? = Firebase.auth.currentUser?.uid

internal inline fun withAuth(
  onError: (String) -> Unit,
  action: (userId: String) -> Unit,
) {
  val userId = currentUserId()
  if (userId != null) action(userId) else onError("User is not available")
}

internal fun Query.toProductListFlow(
  errorMessage: String = "Error while retrieving products",
): Flow<RequestState<List<Product>>> =
  snapshots
    .map<_, RequestState<List<Product>>> { query ->
      RequestState.Success(query.documents.map { it.toProduct() })
    }
    .onStart { emit(RequestState.Loading) }
    .catch { emit(RequestState.Error("$errorMessage: ${it.message}")) }

internal fun authenticatedProductListFlow(
  errorMessage: String = "Error while retrieving products",
  query: () -> Query,
): Flow<RequestState<List<Product>>> {
  if (currentUserId() == null) return flowOf(RequestState.Error("User is not available"))
  return query().toProductListFlow(errorMessage)
}
