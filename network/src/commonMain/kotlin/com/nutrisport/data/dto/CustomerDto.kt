package com.nutrisport.data.dto

import com.nutrisport.shared.domain.CartItem

data class CustomerDto(
  val id: String,
  val firstName: String,
  val lastName: String,
  val email: String,
  val city: String?,
  val postalCode: Int?,
  val address: String?,
  val phoneDialCode: Int?,
  val phoneNumber: String?,
  val cart: List<CartItem>,
  val isAdmin: Boolean,
)
