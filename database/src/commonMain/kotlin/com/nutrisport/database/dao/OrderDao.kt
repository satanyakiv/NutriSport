package com.nutrisport.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nutrisport.database.entity.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

  @Query("SELECT * FROM orders WHERE customerId = :customerId")
  fun observeByCustomerId(customerId: String): Flow<List<OrderEntity>>

  @Upsert
  suspend fun upsert(order: OrderEntity)
}
