package com.nutrisport.data

import com.nutrisport.data.mapper.CustomerDtoToEntityMapper
import com.nutrisport.data.mapper.CustomerEntityToDomainMapper
import com.nutrisport.database.dao.CartItemDao
import com.nutrisport.database.dao.CustomerDao
import com.nutrisport.database.entity.CartItemEntity
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CustomerRepositoryImpl(
  private val customerMapper: CustomerMapper,
  private val dtoToEntity: CustomerDtoToEntityMapper,
  private val entityToDomain: CustomerEntityToDomainMapper,
  private val customerDao: CustomerDao,
  private val cartItemDao: CartItemDao,
) : CustomerRepository {
  private val customerCollection = Firebase.firestore.collection(collectionPath = COLLECTION_NAME)

  companion object {
    private const val COLLECTION_NAME = "customer"
  }
  private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun getCurrentUserId(): String? = currentUserId()

  override suspend fun createCustomer(
    uid: String,
    displayName: String?,
    email: String?,
  ): DomainResult<Unit> {
    return try {
      if (uid.isBlank()) return Either.Left(AppError.Unauthorized("User is null"))

      val customerExists = customerCollection.document(uid).get().exists
      if (customerExists) return Either.Right(Unit)

      val customer = Customer(
        id = uid,
        firstName = displayName?.split(" ")?.firstOrNull() ?: "Unknown",
        lastName = displayName?.split(" ")?.lastOrNull() ?: "Unknown",
        email = email ?: "Unknown",
      )
      customerCollection.document(uid).set(customer)
      customerCollection.document(uid)
        .collection("privateData")
        .document("role")
        .set(mapOf("isAdmin" to false))
      Either.Right(Unit)
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while creating a customer: ${e.message}"))
    }
  }

  override suspend fun signOut(): DomainResult<Unit> {
    return try {
      Firebase.auth.signOut()
      Either.Right(Unit)
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while signing out: ${e.message}"))
    }
  }

  override fun readCustomerFlow(): Flow<DomainResult<Customer>> {
    val userId = currentUserId()
      ?: return flowOf(Either.Left(AppError.Unauthorized()))

    syncCustomerFromFirebase(userId)

    return combine(
      customerDao.observeById(userId),
      cartItemDao.observeByCustomerId(userId),
    ) { customerEntity, cartItems ->
      if (customerEntity != null) {
        Either.Right(entityToDomain.map(customerEntity, cartItems))
      } else {
        Either.Left<AppError>(AppError.Unauthorized("User is not available."))
      }
    }
      .catch {
        emit(Either.Left(AppError.Network("Error while reading Customer information: ${it.message}")))
      }
  }

  override suspend fun updateCustomer(customer: Customer): DomainResult<Unit> {
    return try {
      withAuth { userId ->
        val existingCustomer = customerCollection.document(customer.id).get()
        if (existingCustomer.exists) {
          customerCollection.document(userId).update(customer)
          Either.Right(Unit)
        } else {
          Either.Left(AppError.NotFound("User not found"))
        }
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("Error while updating a customer: ${e.message}"))
    }
  }

  override suspend fun addItemToCart(cartItem: CartItem): DomainResult<Unit> {
    return modifyCart("Error while adding a product to cart") { cart ->
      cart + cartItem
    }
  }

  override suspend fun updateCartItemQuantity(
    id: String,
    quantity: Int,
  ): DomainResult<Unit> {
    return modifyCart("Error while updating a product in cart") { cart ->
      cart.map { if (it.id == id) it.copy(quantity = quantity) else it }
    }
  }

  override suspend fun deleteCartItem(id: String): DomainResult<Unit> {
    return modifyCart("Error while deleting a product from cart") { cart ->
      cart.filterNot { it.id == id }
    }
  }

  override suspend fun deleteAllCartItems(): DomainResult<Unit> {
    return modifyCart("Error while deleting all products from cart") {
      emptyList()
    }
  }

  private suspend inline fun modifyCart(
    errorMessage: String,
    transform: (List<CartItem>) -> List<CartItem>,
  ): DomainResult<Unit> {
    return try {
      withAuth { userId ->
        val document = customerCollection.document(userId).get()
        if (document.exists) {
          val updatedCart = transform(document.get<List<CartItem>>("cart"))
          customerCollection.document(userId)
            .set(data = mapOf("cart" to updatedCart), merge = true)
          Either.Right(Unit)
        } else {
          Either.Left(AppError.NotFound("Customer does not exist."))
        }
      }
    } catch (e: Exception) {
      Either.Left(AppError.Network("$errorMessage: ${e.message}"))
    }
  }

  private fun syncCustomerFromFirebase(userId: String) {
    syncScope.launch {
      try {
        customerCollection.document(userId).snapshots.collect { document ->
          if (document.exists) {
            val privateDataDocument = customerCollection
              .document(userId)
              .collection("privateData")
              .document("role")
              .get()
            val isAdmin = privateDataDocument.get<Boolean>("isAdmin") ?: false
            val dto = customerMapper.map(document, isAdmin)
            customerDao.upsert(dtoToEntity.map(dto))
            cartItemDao.deleteAllByCustomerId(userId)
            val cartEntities = dto.cart.map { cartItem ->
              CartItemEntity(
                id = cartItem.id,
                customerId = userId,
                productId = cartItem.productId,
                flavor = cartItem.flavor,
                quantity = cartItem.quantity,
              )
            }
            if (cartEntities.isNotEmpty()) {
              cartItemDao.upsertAll(cartEntities)
            }
          }
        }
      } catch (e: Exception) {
        Napier.e("Firebase customer sync error: ${e.message}")
      }
    }
  }
}
