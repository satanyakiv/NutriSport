package com.nutrisport.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("customerId")],
)
data class CartItemEntity(
    @PrimaryKey val id: String,
    val customerId: String,
    val productId: String,
    val flavor: String?,
    val quantity: Int,
)
