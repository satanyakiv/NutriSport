package com.nutrisport.shared.test

import com.nutrisport.shared.domain.Order
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either

class FakeOrderRepository : OrderRepository {
  var fakeCurrentUserId: String? = "user-1"
  var createOrderError: String? = null

  override fun getCurrentUserId() = fakeCurrentUserId

  override suspend fun createTheOrder(
    order: Order,
  ): DomainResult<Unit> = createOrderError?.let {
    Either.Left(AppError.Unknown(it))
  } ?: Either.Right(Unit)
}
