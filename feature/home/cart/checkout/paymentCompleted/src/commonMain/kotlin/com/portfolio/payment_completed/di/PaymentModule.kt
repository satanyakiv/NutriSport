package com.portfolio.payment_completed.di

import com.portfolio.payment_completed.PaymentViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val paymentModule = module {
  viewModelOf(::PaymentViewModel)
}
