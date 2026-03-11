package com.nutrisport.checkout.di

import com.nutrisport.checkout.CheckoutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val checkoutModule = module {
  viewModelOf(::CheckoutViewModel)
}
