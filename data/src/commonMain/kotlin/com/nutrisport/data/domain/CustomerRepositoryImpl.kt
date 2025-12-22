package com.nutrisport.data.domain

import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

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
}