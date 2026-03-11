package com.nutrisport.data.di

import com.nutrisport.data.AdminRepositoryImpl
import com.nutrisport.data.CustomerMapper
import com.nutrisport.data.CustomerRepositoryImpl
import com.nutrisport.data.OrderRepositoryImpl
import com.nutrisport.data.ProductMapper
import com.nutrisport.data.ProductRepositoryImpl
import com.nutrisport.data.mapper.CustomerDtoToEntityMapper
import com.nutrisport.data.mapper.CustomerEntityToDomainMapper
import com.nutrisport.data.mapper.OrderToDtoMapper
import com.nutrisport.data.mapper.ProductDtoToDomainMapper
import com.nutrisport.data.mapper.ProductDtoToEntityMapper
import com.nutrisport.data.mapper.ProductEntityToDomainMapper
import com.nutrisport.data.mapper.ProductToDtoMapper
import com.nutrisport.shared.domain.AdminRepository
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.domain.OrderRepository
import com.nutrisport.shared.domain.ProductRepository
import org.koin.dsl.module

val networkModule = module {
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

  // Repositories
  single<CustomerRepository> {
    CustomerRepositoryImpl(get(), get(), get(), get(), get())
  }
  single<AdminRepository> { AdminRepositoryImpl(get(), get(), get()) }
  single<ProductRepository> {
    ProductRepositoryImpl(get(), get(), get(), get())
  }
  single<OrderRepository> { OrderRepositoryImpl(get(), get()) }
}
