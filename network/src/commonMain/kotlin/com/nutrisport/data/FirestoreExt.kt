package com.nutrisport.data

import com.nutrisport.data.dto.ProductDto
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal fun currentUserId(): String? = Firebase.auth.currentUser?.uid

internal inline fun <T> withAuth(action: (userId: String) -> DomainResult<T>): DomainResult<T> {
  val userId = currentUserId()
  return if (userId != null) action(userId) else Either.Left(AppError.Unauthorized())
}

internal suspend inline fun <T> withAdminAuth(
  action: (userId: String) -> DomainResult<T>,
): DomainResult<T> {
  val userId = currentUserId() ?: return Either.Left(AppError.Unauthorized())
  val isAdmin = try {
    Firebase.firestore
      .collection("customer")
      .document(userId)
      .collection("privateData")
      .document("role")
      .get()
      .get<Boolean>("isAdmin")
  } catch (_: Exception) {
    false
  }
  return if (isAdmin) action(userId) else Either.Left(AppError.Unauthorized("Admin access required"))
}

internal fun Query.toProductDtoListFlow(
  mapper: ProductMapper,
  errorMessage: String = "Error while retrieving products",
): Flow<DomainResult<List<ProductDto>>> =
  snapshots
    .map<_, DomainResult<List<ProductDto>>> { query ->
      Either.Right(query.documents.map { mapper.map(it) })
    }
    .catch { emit(Either.Left(AppError.Network("$errorMessage: ${it.message}"))) }

internal fun authenticatedProductDtoListFlow(
  mapper: ProductMapper,
  errorMessage: String = "Error while retrieving products",
  query: () -> Query,
): Flow<DomainResult<List<ProductDto>>> {
  if (currentUserId() == null) return flowOf(Either.Left(AppError.Unauthorized()))
  return query().toProductDtoListFlow(mapper, errorMessage)
}
