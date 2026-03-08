package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.Customer
import com.nutrisport.shared.domain.CustomerRepository

class UpdateCustomerUseCase(
    private val customerRepository: CustomerRepository,
) {
    suspend operator fun invoke(
        customer: Customer,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        customerRepository.updateCustomer(
            customer = customer,
            onSuccess = onSuccess,
            onError = onError,
        )
    }
}
