package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.util.DomainResult

class UpdateCustomerUseCase(
  private val customerRepository: CustomerRepository,
) {
  suspend operator fun invoke(customer: Customer): DomainResult<Unit> {
    return customerRepository.updateCustomer(customer = customer)
  }
}
