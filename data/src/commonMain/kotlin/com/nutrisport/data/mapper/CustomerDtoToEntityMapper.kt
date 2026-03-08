package com.nutrisport.data.mapper

import com.nutrisport.data.dto.CustomerDto
import com.nutrisport.database.entity.CustomerEntity

class CustomerDtoToEntityMapper {
    fun map(dto: CustomerDto): CustomerEntity = CustomerEntity(
        id = dto.id,
        firstName = dto.firstName,
        lastName = dto.lastName,
        email = dto.email,
        city = dto.city,
        postalCode = dto.postalCode,
        address = dto.address,
        phoneDialCode = dto.phoneDialCode,
        phoneNumber = dto.phoneNumber,
        isAdmin = dto.isAdmin,
    )
}
