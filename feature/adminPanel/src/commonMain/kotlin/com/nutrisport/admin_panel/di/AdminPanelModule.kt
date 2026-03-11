package com.nutrisport.admin_panel.di

import com.nutrisport.admin_panel.AdminPanelViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val adminPanelModule = module {
  viewModelOf(::AdminPanelViewModel)
}
