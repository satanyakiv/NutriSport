package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.util.DomainResult

class SignOutUseCase(
  private val customerRepository: CustomerRepository,
) {
  suspend operator fun invoke(): DomainResult<Unit> {
    return customerRepository.signOut()
  }
}
