package com.nutrisport.shared.domain

class FakeOrderRepository : OrderRepository {
    var currentUserId: String? = "user-1"
    var createOrderResult: String? = null

    override fun getCurrentUserId() = currentUserId

    override suspend fun createTheOrder(
        order: Order,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        createOrderResult?.let { onError(it) } ?: onSuccess()
    }
}
