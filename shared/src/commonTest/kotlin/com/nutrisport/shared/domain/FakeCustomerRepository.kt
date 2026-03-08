package com.nutrisport.shared.domain

import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCustomerRepository : CustomerRepository {
    var currentUserId: String? = "user-1"
    val customerFlow = MutableStateFlow<RequestState<Customer>>(RequestState.Loading)

    var createCustomerResult: String? = null
    var updateCustomerResult: String? = null
    var addToCartResult: String? = null
    var updateCartQuantityResult: String? = null
    var deleteCartItemResult: String? = null
    var deleteAllCartItemsResult: String? = null
    var signOutResult: RequestState<Unit> = RequestState.Success(Unit)

    override fun getCurrentUserId() = currentUserId

    override suspend fun createCustomer(
        uid: String,
        displayName: String?,
        email: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createCustomerResult?.let { onError(it) } ?: onSuccess()
    }

    override fun readCustomerFlow(): Flow<RequestState<Customer>> = customerFlow

    override suspend fun updateCustomer(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateCustomerResult?.let { onError(it) } ?: onSuccess()
    }

    override suspend fun addItemToCart(
        cartItem: CartItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        addToCartResult?.let { onError(it) } ?: onSuccess()
    }

    override suspend fun updateCartItemQuantity(
        id: String,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        updateCartQuantityResult?.let { onError(it) } ?: onSuccess()
    }

    override suspend fun deleteCartItem(
        id: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        deleteCartItemResult?.let { onError(it) } ?: onSuccess()
    }

    override suspend fun deleteAllCartItems(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        deleteAllCartItemsResult?.let { onError(it) } ?: onSuccess()
    }

    override suspend fun signOut() = signOutResult
}
