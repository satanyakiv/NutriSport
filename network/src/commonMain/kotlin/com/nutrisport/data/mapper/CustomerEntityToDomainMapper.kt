package com.nutrisport.data.mapper

import com.nutrisport.database.entity.CartItemEntity
import com.nutrisport.database.entity.CustomerEntity
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.PhoneNumber

class CustomerEntityToDomainMapper {
  fun map(entity: CustomerEntity, cartItems: List<CartItemEntity>): Customer = Customer(
    id = entity.id,
    firstName = entity.firstName,
    lastName = entity.lastName,
    email = entity.email,
    city = entity.city,
    postalCode = entity.postalCode,
    address = entity.address,
    phoneNumber = run {
      val dialCode = entity.phoneDialCode
      val number = entity.phoneNumber
      if (dialCode != null && number != null) PhoneNumber(dialCode, number) else null
    },
    cart = cartItems.map { mapCartItem(it) },
    isAdmin = entity.isAdmin,
  )

  private fun mapCartItem(entity: CartItemEntity): CartItem = CartItem(
    id = entity.id,
    productId = entity.productId,
    flavor = entity.flavor,
    quantity = entity.quantity,
  )
}
