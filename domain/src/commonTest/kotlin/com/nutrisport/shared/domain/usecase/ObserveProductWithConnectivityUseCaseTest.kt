package com.nutrisport.shared.domain.usecase

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.nutrisport.shared.domain.ConnectivityStatus
import com.nutrisport.shared.test.FakeConnectivityObserver
import com.nutrisport.shared.test.FakeProductRepository
import com.nutrisport.shared.test.fakeProduct
import com.nutrisport.shared.util.Either
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ObserveProductWithConnectivityUseCaseTest {

  private val fakeProductRepo = FakeProductRepository()
  private val fakeConnectivity = FakeConnectivityObserver()

  private val useCase = ObserveProductWithConnectivityUseCase(
    productRepository = fakeProductRepo,
    connectivityObserver = fakeConnectivity,
  )

  @Test
  fun `should combine product and connectivity flows`() = runTest {
    fakeProductRepo.productByIdFlows = mutableMapOf(
      "prod-1" to flowOf(Either.Right(fakeProduct())),
    )

    useCase("prod-1").test {
      val result = awaitItem()
      assertThat(result.connectivity).isEqualTo(ConnectivityStatus.Available)
      assertThat(result.product.getOrNull()).isNotNull()
      assertThat(result.product.getOrNull()!!.title).isEqualTo("WHEY PROTEIN")
    }
  }

  @Test
  fun `should emit Unavailable when connectivity drops`() = runTest {
    fakeProductRepo.productByIdFlows = mutableMapOf(
      "prod-1" to flowOf(Either.Right(fakeProduct())),
    )
    fakeConnectivity.emit(ConnectivityStatus.Unavailable)

    useCase("prod-1").test {
      val result = awaitItem()
      assertThat(result.connectivity).isEqualTo(ConnectivityStatus.Unavailable)
      assertThat(result.product.getOrNull()).isNotNull()
    }
  }
}
