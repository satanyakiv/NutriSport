package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.PhoneNumber

class ValidateProfileFormUseCase {
    operator fun invoke(
        firstName: String,
        lastName: String,
        city: String?,
        postalCode: Int?,
        address: String?,
        phoneNumber: PhoneNumber?,
    ): Boolean {
        return firstName.length in 3..50 &&
            lastName.length in 3..50 &&
            city?.length in 3..50 &&
            (postalCode != null && postalCode.toString().length in 3..8) &&
            address?.length in 3..50 &&
            phoneNumber?.number?.length in 5..30
    }
}
