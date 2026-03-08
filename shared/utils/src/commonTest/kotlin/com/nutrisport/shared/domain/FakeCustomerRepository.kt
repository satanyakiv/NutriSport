package com.nutrisport.shared.domain

import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCustomerRepository : CustomerRepository {
    var fakeCurrentUserId: String? = "user-1"
    val customerFlow = MutableStateFlow<DomainResult<Customer>>(
        Either.Right(fakeCustomer()),
    )

    var createCustomerError: String? = null
    var updateCustomerError: String? = null
    var addToCartError: String? = null
    var updateCartQuantityError: String? = null
    var deleteCartItemError: String? = null
    var deleteAllCartItemsError: String? = null
    var signOutResult: DomainResult<Unit> = Either.Right(Unit)

    override fun getCurrentUserId() = fakeCurrentUserId

    override suspend fun createCustomer(
        uid: String,
        displayName: String?,
        email: String?,
    ): DomainResult<Unit> = createCustomerError?.let {
        Either.Left(AppError.Unknown(it))
    } ?: Either.Right(Unit)

    override fun readCustomerFlow(): Flow<DomainResult<Customer>> = customerFlow

    override suspend fun updateCustomer(
        customer: Customer,
    ): DomainResult<Unit> = updateCustomerError?.let {
        Either.Left(AppError.Unknown(it))
    } ?: Either.Right(Unit)

    override suspend fun addItemToCart(
        cartItem: CartItem,
    ): DomainResult<Unit> = addToCartError?.let {
        Either.Left(AppError.Unknown(it))
    } ?: Either.Right(Unit)

    override suspend fun updateCartItemQuantity(
        id: String,
        quantity: Int,
    ): DomainResult<Unit> = updateCartQuantityError?.let {
        Either.Left(AppError.Unknown(it))
    } ?: Either.Right(Unit)

    override suspend fun deleteCartItem(
        id: String,
    ): DomainResult<Unit> = deleteCartItemError?.let {
        Either.Left(AppError.Unknown(it))
    } ?: Either.Right(Unit)

    override suspend fun deleteAllCartItems(): DomainResult<Unit> =
        deleteAllCartItemsError?.let {
            Either.Left(AppError.Unknown(it))
        } ?: Either.Right(Unit)

    override suspend fun signOut() = signOutResult
}
