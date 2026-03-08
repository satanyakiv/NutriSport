package com.nutrisport.data

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
) : OrderRepository {
    override fun getCurrentUserId(): String? = currentUserId()

    override suspend fun createTheOrder(order: Order): DomainResult<Unit> {
        return try {
            withAuth { _ ->
                Firebase.firestore.collection(collectionPath = "order")
                    .document(order.id).set(order)
                customerRepository.deleteAllCartItems()
                Either.Right(Unit)
            }
        } catch (e: Exception) {
            Either.Left(AppError.Network("Error while creating the order: ${e.message}"))
        }
    }
}
