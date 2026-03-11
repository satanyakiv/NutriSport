package com.nutrisport.home.di

import com.nutrisport.home.HomeGraphViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {
  viewModelOf(::HomeGraphViewModel)
}
