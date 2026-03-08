package com.nutrisport.shared.domain

interface OrderRepository {
  fun getCurrentUserId(): String?
  suspend fun createTheOrder(
    order: Order,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  )
}
