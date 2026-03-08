package com.nutrisport.data

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class CustomerRepositoryImpl : CustomerRepository {
  private val customerCollection = Firebase.firestore.collection(collectionPath = "customer")

  override fun getCurrentUserId(): String? = currentUserId()

  override suspend fun createCustomer(
    uid: String,
    displayName: String?,
    email: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      if (uid.isBlank()) { onError("User is null"); return }

      val customerExists = customerCollection.document(uid).get().exists
      if (customerExists) { onSuccess(); return }

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
      onSuccess()
    } catch (e: Exception) {
      onError("Error while creating a customer: ${e.message}")
    }
  }

  override suspend fun signOut(): RequestState<Unit> {
    return try {
      Firebase.auth.signOut()
      RequestState.Success(Unit)
    } catch (e: Exception) {
      RequestState.Error("Error while signing out: ${e.message}")
    }
  }

  override fun readCustomerFlow(): Flow<RequestState<Customer>> {
    val userId = currentUserId()
      ?: return flowOf(RequestState.Error("User is not available"))
    return customerCollection.document(userId).snapshots
      .map { document ->
        if (document.exists) {
          val privateDataDocument = customerCollection
            .document(userId)
            .collection("privateData")
            .document("role")
            .get()

          val customer = Customer(
            id = document.id,
            firstName = document.get("firstName"),
            lastName = document.get("lastName"),
            email = document.get("email"),
            city = document.get("city"),
            postalCode = document.get("postalCode"),
            address = document.get("address"),
            phoneNumber = document.get("phoneNumber"),
            cart = document.get("cart"),
            isAdmin = privateDataDocument.get("isAdmin") ?: false,
          )
          RequestState.Success(customer)
        } else {
          RequestState.Error("User is not available.")
        }
      }
      .onStart { emit(RequestState.Loading) }
      .catch { emit(RequestState.Error("Error while reading Customer information: ${it.message}")) }
  }

  override suspend fun updateCustomer(
    customer: Customer,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      withAuth(onError) { userId ->
        val existingCustomer = customerCollection.document(customer.id).get()
        if (existingCustomer.exists) {
          customerCollection.document(userId).update(customer)
          onSuccess()
        } else {
          onError("User not found")
        }
      }
    } catch (e: Exception) {
      onError("Error while updating a customer: ${e.message}")
    }
  }

  override suspend fun addItemToCart(
    cartItem: CartItem,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    modifyCart(onSuccess, onError, "Error while adding a product to cart") { cart ->
      cart + cartItem
    }
  }

  override suspend fun updateCartItemQuantity(
    id: String,
    quantity: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    modifyCart(onSuccess, onError, "Error while updating a product in cart") { cart ->
      cart.map { if (it.id == id) it.copy(quantity = quantity) else it }
    }
  }

  override suspend fun deleteCartItem(
    id: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    modifyCart(onSuccess, onError, "Error while deleting a product from cart") { cart ->
      cart.filterNot { it.id == id }
    }
  }

  override suspend fun deleteAllCartItems(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    modifyCart(onSuccess, onError, "Error while deleting all products from cart") {
      emptyList()
    }
  }

  private suspend inline fun modifyCart(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
    errorMessage: String,
    transform: (List<CartItem>) -> List<CartItem>,
  ) {
    try {
      withAuth(onError) { userId ->
        val document = customerCollection.document(userId).get()
        if (document.exists) {
          val updatedCart = transform(document.get<List<CartItem>>("cart"))
          customerCollection.document(userId)
            .set(data = mapOf("cart" to updatedCart), merge = true)
          onSuccess()
        } else {
          onError("Customer does not exist.")
        }
      }
    } catch (e: Exception) {
      onError("$errorMessage: ${e.message}")
    }
  }
}
