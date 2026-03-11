package com.nutrisport.details.di

import com.nutrisport.details.DetailsViewModel
import com.nutrisport.details.mapper.ProductToUiMapper
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val detailsModule = module {
  factory { ProductToUiMapper() }
  viewModelOf(::DetailsViewModel)
}
