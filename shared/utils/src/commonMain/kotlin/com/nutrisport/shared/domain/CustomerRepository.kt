package com.nutrisport.shared.domain

import com.nutrisport.shared.util.DomainResult
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCurrentUserId(): String?
    suspend fun createCustomer(
        uid: String,
        displayName: String?,
        email: String?,
    ): DomainResult<Unit>
    fun readCustomerFlow(): Flow<DomainResult<Customer>>
    suspend fun updateCustomer(customer: Customer): DomainResult<Unit>
    suspend fun addItemToCart(cartItem: CartItem): DomainResult<Unit>
    suspend fun updateCartItemQuantity(id: String, quantity: Int): DomainResult<Unit>
    suspend fun deleteCartItem(id: String): DomainResult<Unit>
    suspend fun deleteAllCartItems(): DomainResult<Unit>
    suspend fun signOut(): DomainResult<Unit>
}
