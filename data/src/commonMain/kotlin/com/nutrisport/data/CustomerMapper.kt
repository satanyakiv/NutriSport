package com.nutrisport.data

import com.nutrisport.data.dto.CustomerDto
import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.PhoneNumber
import dev.gitlive.firebase.firestore.DocumentSnapshot

class CustomerMapper {
    fun map(document: DocumentSnapshot, isAdmin: Boolean): CustomerDto {
        val phoneNumber: PhoneNumber? = document.get("phoneNumber")
        return CustomerDto(
            id = document.id,
            firstName = document.get("firstName"),
            lastName = document.get("lastName"),
            email = document.get("email"),
            city = document.get("city"),
            postalCode = document.get("postalCode"),
            address = document.get("address"),
            phoneDialCode = phoneNumber?.dialCode,
            phoneNumber = phoneNumber?.number,
            cart = document.get<List<CartItem>>("cart"),
            isAdmin = isAdmin,
        )
    }
}
