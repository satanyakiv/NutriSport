package com.nutrisport.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nutrisport.database.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartItemDao {

    @Query("SELECT * FROM cart_items WHERE customerId = :customerId")
    fun observeByCustomerId(customerId: String): Flow<List<CartItemEntity>>

    @Upsert
    suspend fun upsert(cartItem: CartItemEntity)

    @Upsert
    suspend fun upsertAll(cartItems: List<CartItemEntity>)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cart_items WHERE customerId = :customerId")
    suspend fun deleteAllByCustomerId(customerId: String)
}
