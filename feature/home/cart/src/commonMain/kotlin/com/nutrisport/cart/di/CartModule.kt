package com.nutrisport.cart.di

import com.nutrisport.cart.CartViewModel
import com.nutrisport.cart.mapper.CartItemToUiMapper
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val cartModule = module {
  factory { CartItemToUiMapper() }
  viewModelOf(::CartViewModel)
}
