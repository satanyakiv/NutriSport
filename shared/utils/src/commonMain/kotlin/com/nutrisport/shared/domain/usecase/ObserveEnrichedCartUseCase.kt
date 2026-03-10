package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveEnrichedCartUseCase(
  private val customerRepository: CustomerRepository,
  private val productRepository: ProductRepository,
  private val enrichCartWithProductsUseCase: EnrichCartWithProductsUseCase,
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<DomainResult<List<Pair<CartItem, Product>>>> {
    val customerFlow = customerRepository.readCustomerFlow()

    val productsFlow = customerFlow.flatMapLatest { customerResult ->
      customerResult.fold(
        ifLeft = { flowOf(Either.Left(it)) },
        ifRight = { customer ->
          val productIds = customer.cart.map { it.productId }.toSet()
          if (productIds.isNotEmpty()) {
            productRepository.readProductsByIdsFlow(productIds.toList())
          } else {
            flowOf(Either.Right(emptyList()))
          }
        },
      )
    }

    return combine(customerFlow, productsFlow) { customerResult, productsResult ->
      customerResult.fold(
        ifLeft = { Either.Left(it) },
        ifRight = { customer ->
          productsResult.fold(
            ifLeft = { Either.Left(it) },
            ifRight = { products ->
              Either.Right(
                enrichCartWithProductsUseCase(customer.cart, products),
              )
            },
          )
        },
      )
    }
  }
}
