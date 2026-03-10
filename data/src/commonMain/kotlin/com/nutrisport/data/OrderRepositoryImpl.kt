package com.nutrisport.data

import com.nutrisport.data.mapper.OrderToDtoMapper
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Order
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class OrderRepositoryImpl(
  private val customerRepository: CustomerRepository,
  private val orderToDtoMapper: OrderToDtoMapper,
) : OrderRepository {
  companion object {
    private const val COLLECTION_NAME = "order"
  }

  override fun getCurrentUserId(): String? = currentUserId()

  override suspend fun createTheOrder(order: Order): DomainResult<Unit> {
    return try {
      withAuth { _ ->
        val dto = orderToDtoMapper.map(order)
        Firebase.firestore.collection(collectionPath = COLLECTION_NAME)
          .document(order.id).set(dto)
        customerRepository.deleteAllCartItems()
        Either.Right(Unit)
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while creating the order: ${e.message}"))
    }
  }
}
