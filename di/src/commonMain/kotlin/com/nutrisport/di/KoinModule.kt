package com.nutrisport.di

import com.nutrisport.admin_panel.AdminPanelViewModel
import com.nutrisport.auth.component.AuthViewModel
import com.nutrisport.cart.CartViewModel
import com.nutrisport.cart.mapper.CartItemToUiMapper
import com.nutrisport.checkout.CheckoutViewModel
import com.nutrisport.data.AdminRepositoryImpl
import com.nutrisport.data.CustomerMapper
import com.nutrisport.data.CustomerRepositoryImpl
import com.nutrisport.data.OrderRepositoryImpl
import com.nutrisport.data.ProductMapper
import com.nutrisport.data.ProductRepositoryImpl
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.data.mapper.CustomerDtoToEntityMapper
import com.nutrisport.data.mapper.CustomerEntityToDomainMapper
import com.nutrisport.data.mapper.OrderToDtoMapper
import com.nutrisport.data.mapper.ProductDtoToDomainMapper
import com.nutrisport.data.mapper.ProductDtoToEntityMapper
import com.nutrisport.data.mapper.ProductEntityToDomainMapper
import com.nutrisport.data.mapper.ProductToDtoMapper
import com.nutrisport.database.NutriSportDatabase
import com.nutrisport.details.DetailsViewModel
import com.nutrisport.details.mapper.ProductToUiMapper
import com.nutrisport.home.HomeGraphViewModel
import com.nutrisport.manage_product.ManageProductViewModel
import com.nutrisport.products_overview.ProductsOverviewViewModel
import com.nutrisport.profile.ProfileViewModel
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.domain.usecase.CalculateCartTotalUseCase
import com.nutrisport.shared.domain.usecase.CreateOrderUseCase
import com.nutrisport.shared.domain.usecase.EnrichCartWithProductsUseCase
import com.nutrisport.shared.domain.usecase.ObserveEnrichedCartUseCase
import com.nutrisport.shared.domain.usecase.SignOutUseCase
import com.nutrisport.shared.domain.usecase.UpdateCustomerUseCase
import com.nutrisport.shared.domain.usecase.ValidateProfileFormUseCase
import com.portfolio.categories_search.CategorySearchViewModel
import com.portfolio.payment_completed.PaymentViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
  // Database DAOs
  single { get<NutriSportDatabase>().productDao() }
  single { get<NutriSportDatabase>().customerDao() }
  single { get<NutriSportDatabase>().cartItemDao() }
  single { get<NutriSportDatabase>().orderDao() }

  // Mappers — Firebase
  factory { ProductMapper() }
  factory { CustomerMapper() }

  // Mappers — DTO ↔ Entity ↔ Domain
  factory { ProductDtoToEntityMapper() }
  factory { ProductEntityToDomainMapper() }
  factory { ProductDtoToDomainMapper() }
  factory { ProductToDtoMapper() }
  factory { OrderToDtoMapper() }
  factory { CustomerDtoToEntityMapper() }
  factory { CustomerEntityToDomainMapper() }

  // Mappers — Domain → UI
  factory { ProductToUiMapper() }
  factory { CartItemToUiMapper() }

  // Repositories
  single<CustomerRepository> {
    CustomerRepositoryImpl(get(), get(), get(), get(), get())
  }
  single<AdminRepository> { AdminRepositoryImpl(get(), get(), get()) }
  single<ProductRepository> {
    ProductRepositoryImpl(get(), get(), get(), get())
  }
  single<OrderRepository> { OrderRepositoryImpl(get(), get()) }

  // UseCases
  factory { CalculateCartTotalUseCase() }
  factory { EnrichCartWithProductsUseCase() }
  factory { ValidateProfileFormUseCase() }
  factory { SignOutUseCase(get()) }
  factory { CreateOrderUseCase(get()) }
  factory { UpdateCustomerUseCase(get()) }
  factory { ObserveEnrichedCartUseCase(get(), get(), get()) }

  // ViewModels
  viewModelOf(::AuthViewModel)
  viewModelOf(::HomeGraphViewModel)
  viewModelOf(::ProfileViewModel)
  viewModelOf(::ManageProductViewModel)
  viewModelOf(::AdminPanelViewModel)
  viewModelOf(::ProductsOverviewViewModel)
  viewModelOf(::DetailsViewModel)
  viewModelOf(::CartViewModel)
  viewModelOf(::CategorySearchViewModel)
  viewModelOf(::CheckoutViewModel)
  viewModelOf(::PaymentViewModel)
}

expect val targetModule: Module

fun initializeKoin(
  config: (KoinApplication.() -> Unit)? = null,
) {
  startKoin {
    config?.invoke(this)
    modules(
      sharedModule,
      targetModule,
    )
  }
}
