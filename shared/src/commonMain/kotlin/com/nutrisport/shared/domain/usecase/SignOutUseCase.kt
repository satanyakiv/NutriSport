package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.util.RequestState

class SignOutUseCase(
    private val customerRepository: CustomerRepository,
) {
    suspend operator fun invoke(): RequestState<Unit> {
        return customerRepository.signOut()
    }
}
