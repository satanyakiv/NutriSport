package com.nutrisport.auth.di

import com.nutrisport.auth.component.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
  viewModelOf(::AuthViewModel)
}
