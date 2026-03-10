package com.nutrisport.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
  @PrimaryKey val id: String,
  val firstName: String,
  val lastName: String,
  val email: String,
  val city: String?,
  val postalCode: Int?,
  val address: String?,
  val phoneDialCode: Int?,
  val phoneNumber: String?,
  val isAdmin: Boolean,
)
