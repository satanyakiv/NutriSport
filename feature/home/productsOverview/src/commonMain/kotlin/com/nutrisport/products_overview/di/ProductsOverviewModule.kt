package com.nutrisport.products_overview.di

import com.nutrisport.products_overview.ProductsOverviewViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val productsOverviewModule = module {
  viewModelOf(::ProductsOverviewViewModel)
}
