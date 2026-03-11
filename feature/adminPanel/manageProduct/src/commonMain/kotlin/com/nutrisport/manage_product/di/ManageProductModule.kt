package com.nutrisport.manage_product.di

import com.nutrisport.manage_product.ManageProductViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val manageProductModule = module {
  viewModelOf(::ManageProductViewModel)
}
