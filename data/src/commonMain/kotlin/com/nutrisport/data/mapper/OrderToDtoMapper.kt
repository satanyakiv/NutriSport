package com.nutrisport.data.mapper

import com.nutrisport.data.dto.OrderDto
import com.nutrisport.shared.domain.Order

class OrderToDtoMapper {
    fun map(order: Order): OrderDto = OrderDto(
        id = order.id,
        customerId = order.customerId,
        items = order.items,
        totalAmount = order.totalAmount,
        token = order.token,
    )
}
