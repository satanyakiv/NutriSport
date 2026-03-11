package com.portfolio.categories_search.di

import com.portfolio.categories_search.CategorySearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val categorySearchModule = module {
  viewModelOf(::CategorySearchViewModel)
}
