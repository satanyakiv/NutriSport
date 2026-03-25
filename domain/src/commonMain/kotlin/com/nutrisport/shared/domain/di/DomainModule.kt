package com.nutrisport.shared.domain.di

import com.nutrisport.shared.domain.usecase.CalculateCartTotalUseCase
import com.nutrisport.shared.domain.usecase.CreateOrderUseCase
import com.nutrisport.shared.domain.usecase.EnrichCartWithProductsUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.domain.usecase.ObserveProductWithConnectivityUseCase
import com.nutrisport.shared.domain.usecase.SignOutUseCase
import com.nutrisport.shared.domain.usecase.UpdateCustomerUseCase
import com.nutrisport.shared.domain.usecase.ValidateCartPricesUseCase
import com.nutrisport.shared.domain.usecase.ValidateProfileFormUseCase
import org.koin.dsl.module

val domainModule = module {
  factory { CalculateCartTotalUseCase() }
  factory { EnrichCartWithProductsUseCase() }
  factory { ValidateProfileFormUseCase() }
  factory { ValidateCartPricesUseCase() }
  factory { SignOutUseCase(get()) }
  factory { CreateOrderUseCase(get()) }
  factory { UpdateCustomerUseCase(get()) }
  factory { ObserveEnrichedCartUseCase(get(), get(), get()) }
  factory { ObserveProductWithConnectivityUseCase(get(), get()) }
}
