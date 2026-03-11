package com.nutrisport.profile.di

import com.nutrisport.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
  viewModelOf(::ProfileViewModel)
}
