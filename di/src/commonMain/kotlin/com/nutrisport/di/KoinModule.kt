package com.nutrisport.di

import com.nutrisport.auth.component.AuthViewModel
import com.nutrisport.data.domain.CustomerRepository
import com.nutrisport.data.domain.CustomerRepositoryImpl
import com.nutrisport.home.HomeGraphViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val shared = module {
  single<CustomerRepository> { CustomerRepositoryImpl() }
  viewModelOf(::AuthViewModel)
  viewModelOf(::HomeGraphViewModel)
}

fun initializeKoin(
  config: (KoinApplication.() -> Unit)? = null,
) {
  startKoin {
    config?.invoke(this)
    modules(
      shared
    )
  }
}