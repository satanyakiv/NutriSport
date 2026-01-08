package com.nutrisport.di

import com.nutrisport.admin_panel.AdminPanelVewModel
import com.nutrisport.auth.component.AuthViewModel
import com.nutrisport.data.CustomerRepositoryImpl
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.data.domain.AdminRepositoryImpl
import com.nutrisport.data.domain.CustomerRepository
import com.nutrisport.home.HomeGraphViewModel
import com.nutrisport.manage_product.ManageProductViewModule
import com.nutrisport.profile.ProfileViewModel
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val sharedModule = module {
  single<CustomerRepository> { CustomerRepositoryImpl() }
  single<AdminRepository> { AdminRepositoryImpl() }

  viewModelOf(::AuthViewModel)
  viewModelOf(::HomeGraphViewModel)
  viewModelOf(::ProfileViewModel)
  viewModelOf(::ManageProductViewModule)
  viewModelOf(::AdminPanelVewModel)
}

expect val targetModule: Module

fun initializeKoin(
  config: (KoinApplication.() -> Unit)? = null,
) {
  startKoin {
    config?.invoke(this)
    modules(
      sharedModule,
      targetModule
    )
  }
}