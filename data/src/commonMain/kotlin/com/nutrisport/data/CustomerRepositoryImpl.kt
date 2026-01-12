package com.nutrisport.data

import com.nutrisport.data.domain.CustomerRepository
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class CustomerRepositoryImpl : CustomerRepository {
  override suspend fun createCustomer(
    user: FirebaseUser?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      if (user != null) {
        val customerCollection = Firebase.firestore.collection(collectionPath = "customer")
        val customer = Customer(
          id = user.uid,
          firstName = user.displayName?.split(" ")?.firstOrNull() ?: "Unknown",
          lastName = user.displayName?.split(" ")?.lastOrNull() ?: "Unknown",
          email = user.email ?: "Unknown",
          //will allow user update it's information from profile
        )

        val customerExists = customerCollection.document(user.uid).get().exists
        if (customerExists) {
          onSuccess()
        } else {
          customerCollection.document(user.uid).set(customer)
          customerCollection.document(user.uid)
            .collection("privateData")
            .document("role")
            .set(mapOf("isAdmin" to false))
          onSuccess()
        }
      } else {
        onError("User is null")
      }
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

  override fun getCurrentUserId(): String? {
    return Firebase.auth.currentUser?.uid
  }

  override fun readCustomerFlow(): Flow<RequestState<Customer>> {
    val userId = getCurrentUserId()
      ?: return flowOf(RequestState.Error("User is not available"))
    return Firebase.firestore.collection(collectionPath = "customer")
      .document(userId)
      .snapshots
      .map { document ->
        if (document.exists) {
          val privateDataDocument = Firebase.firestore.collection(collectionPath = "customer")
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
            isAdmin = privateDataDocument.get("isAdmin") ?: false
          )
          RequestState.Success(customer)
        } else {
          RequestState.Error("User is not available.")
        }
      }
      .onStart { emit(RequestState.Loading) }
      .catch { e ->
        emit(RequestState.Error("Error while reading Customer information: ${e.message}"))
      }
  }

  override suspend fun updateCustomer(
    customer: Customer,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val userId = getCurrentUserId()
      if (userId != null) {
        val firestore = Firebase.firestore
        val customerCollection = firestore.collection(collectionPath = "customer")
        val existingCustomer = customerCollection.document(customer.id).get()
        if (existingCustomer.exists) {
          customerCollection.document(userId).update(customer)
          onSuccess()
        } else {
          onError("User not found")
        }
      } else {
        onError("User is not available")
      }
    } catch (e: Exception) {
      onError("Error while updating a customer: ${e.message}")
    }
  }

  override suspend fun addItemToCard(
    cartItem: CartItem,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      val currentUserId = getCurrentUserId()
      if (currentUserId != null) {
        val database = Firebase.firestore
        val customerCollection = database.collection(collectionPath = "customer")

        val existingCustomer = customerCollection
          .document(currentUserId)
          .get()
        if (existingCustomer.exists) {
          val existingCart = existingCustomer.get<List<CartItem>>("cart")
          val updatedCart = existingCart + cartItem
          customerCollection.document(currentUserId)
            .set(
              data = mapOf("cart" to updatedCart),
              merge = true
            )
          onSuccess()
        } else {
          onError("Select customer does not exist.")
        }
      } else {
        onError("User is not available.")
      }
    } catch (e: Exception) {
      onError("Error while adding a product to cart: ${e.message}")
    }
  }

  override suspend fun updateCartItemQuantity(
    id: String,
    quantity: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      val currentUserId = getCurrentUserId()
      if (currentUserId != null) {
        val database = Firebase.firestore
        val customerCollection = database.collection(collectionPath = "customer")

        val existingCustomer = customerCollection
          .document(currentUserId)
          .get()
        if (existingCustomer.exists) {
          val existingCart = existingCustomer.get<List<CartItem>>("cart")
          val updatedCart = existingCart.map { cartItem ->
            if (cartItem.id == id) {
              cartItem.copy(quantity = quantity)
            } else cartItem
          }
          customerCollection.document(currentUserId)
            .update(data = mapOf("cart" to updatedCart))
          onSuccess()
        } else {
          onError("Select customer does not exist.")
        }
      } else {
        onError("User is not available.")
      }
    } catch (e: Exception) {
      onError("Error while updating a product to cart: ${e.message}")
    }
  }

  override suspend fun deleteCartItem(
    id: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      val currentUserId = getCurrentUserId()
      if (currentUserId != null) {
        val database = Firebase.firestore
        val customerCollection = database.collection(collectionPath = "customer")

        val existingCustomer = customerCollection
          .document(currentUserId)
          .get()
        if (existingCustomer.exists) {
          val existingCart = existingCustomer.get<List<CartItem>>("cart")
          val updatedCart = existingCart.filterNot { it.id == id }
          customerCollection.document(currentUserId)
            .update(data = mapOf("cart" to updatedCart))
          onSuccess()
        } else {
          onError("Select customer does not exist.")
        }
      } else {
        onError("User is not available.")
      }
    } catch (e: Exception) {
      onError("Error while deleting a product from cart: ${e.message}")
    }
  }

  override suspend fun deleteAllCartItems(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    try {
      val currentUserId = getCurrentUserId()
      if (currentUserId != null) {
        val database = Firebase.firestore
        val customerCollection = database.collection(collectionPath = "customer")

        val existingCustomer = customerCollection
          .document(currentUserId)
          .get()
        if (existingCustomer.exists) {
          customerCollection.document(currentUserId)
            .update(data = mapOf("cart" to emptyList<List<CartItem>>()))
          onSuccess()
        } else {
          onError("Select customer does not exist.")
        }
      } else {
        onError("User is not available.")
      }
    } catch (e: Exception) {
      onError("Error while deleting all products from cart: ${e.message}")
    }
  }
}