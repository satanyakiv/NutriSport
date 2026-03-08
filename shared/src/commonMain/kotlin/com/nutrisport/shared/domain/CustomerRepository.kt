package com.nutrisport.shared.domain

import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
  fun getCurrentUserId(): String?
  suspend fun createCustomer(
    uid: String,
    displayName: String?,
    email: String?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  )
  fun readCustomerFlow(): Flow<RequestState<Customer>>
  suspend fun updateCustomer(
    customer: Customer,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  )
  suspend fun addItemToCart(
    cartItem: CartItem,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  )
  suspend fun updateCartItemQuantity(
    id: String,
    quantity: Int,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  )
  suspend fun deleteCartItem(
    id: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  )
  suspend fun deleteAllCartItems(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  )
  suspend fun signOut(): RequestState<Unit>
}
