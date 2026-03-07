package com.nutrisport.data

import com.nutrisport.data.domain.CustomerRepository
import com.nutrisport.data.domain.OrderRepository
import com.nutrisport.shared.domain.Order
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class OrderRepositoryImpl(
  private val customerRepository: CustomerRepository,
) : OrderRepository {
  override fun getCurrentUserId(): String? = currentUserId()

  override suspend fun createTheOrder(
    order: Order,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      withAuth(onError) {
        Firebase.firestore.collection(collectionPath = "order")
          .document(order.id).set(order)
        customerRepository.deleteAllCartItems(onSuccess = {}, onError = {})
        onSuccess()
      }
    } catch (e: Exception) {
      onError("Error while creating the order: ${e.message}")
    }
  }
}
