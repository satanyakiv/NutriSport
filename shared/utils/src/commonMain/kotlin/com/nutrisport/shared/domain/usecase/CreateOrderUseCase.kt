package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Order
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.util.DomainResult

class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
) {
    suspend operator fun invoke(
        customerId: String,
        cartItems: List<CartItem>,
        totalAmount: Double,
    ): DomainResult<Unit> {
        val order = Order(
            customerId = customerId,
            items = cartItems.map { it.copy() },
            totalAmount = totalAmount,
        )
        return orderRepository.createTheOrder(order = order)
    }
}
