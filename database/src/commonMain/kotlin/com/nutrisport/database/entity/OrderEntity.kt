package com.nutrisport.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
  @PrimaryKey val id: String,
  val customerId: String,
  val items: String,
  val totalAmount: Double,
  val token: String?,
)
