package com.nutrisport.shared.domain.usecase

import com.nutrisport.shared.domain.ConnectivityObserver
import com.nutrisport.shared.domain.ConnectivityStatus
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ProductWithConnectivity(
  val product: DomainResult<Product>,
  val connectivity: ConnectivityStatus,
)

class ObserveProductWithConnectivityUseCase(
  private val productRepository: ProductRepository,
  private val connectivityObserver: ConnectivityObserver,
) {
  operator fun invoke(productId: String): Flow<ProductWithConnectivity> {
    return combine(
      productRepository.readProductByIdFlow(productId),
      connectivityObserver.status,
    ) { productResult, connectivity ->
      ProductWithConnectivity(
        product = productResult,
        connectivity = connectivity,
      )
    }
  }
}
