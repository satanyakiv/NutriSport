package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Order
import com.nutrisport.shared.domain.OrderRepository

class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(
        customerId: String,
        cartItems: List<CartItem>,
        totalAmount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val order = Order(
            customerId = customerId,
            items = cartItems.map { it.copy() },
            totalAmount = totalAmount,
        )
        orderRepository.createTheOrder(
            order = order,
            onSuccess = onSuccess,
            onError = onError,
        )
    }
}
