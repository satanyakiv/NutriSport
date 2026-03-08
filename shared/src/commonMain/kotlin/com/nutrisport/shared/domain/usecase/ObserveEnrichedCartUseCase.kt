package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.CartItem
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.RequestState
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
    operator fun invoke(): Flow<RequestState<List<Pair<CartItem, Product>>>> {
        val customerFlow = customerRepository.readCustomerFlow()

        val productsFlow = customerFlow.flatMapLatest { customerState ->
            if (customerState.isSuccess()) {
                val productIds = customerState.getSuccessData().cart.map { it.productId }.toSet()
                if (productIds.isNotEmpty()) {
                    productRepository.readProductsByIdsFlow(productIds.toList())
                } else flowOf(RequestState.Success(emptyList()))
            } else if (customerState.isError()) {
                flowOf(RequestState.Error(customerState.getErrorMessage()))
            } else flowOf(RequestState.Loading)
        }

        return combine(customerFlow, productsFlow) { customerState, productsState ->
            when {
                customerState.isSuccess() && productsState.isSuccess() -> {
                    val cart = customerState.getSuccessData().cart
                    val productList = productsState.getSuccessData()
                    RequestState.Success(enrichCartWithProductsUseCase(cart, productList))
                }
                customerState.isError() -> RequestState.Error(customerState.getErrorMessage())
                productsState.isError() -> RequestState.Error(productsState.getErrorMessage())
                else -> RequestState.Loading
            }
        }
    }
}
