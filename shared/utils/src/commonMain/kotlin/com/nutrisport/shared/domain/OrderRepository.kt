package com.nutrisport.shared.domain

import com.nutrisport.shared.util.DomainResult

interface OrderRepository {
    fun getCurrentUserId(): String?
    suspend fun createTheOrder(order: Order): DomainResult<Unit>
}
