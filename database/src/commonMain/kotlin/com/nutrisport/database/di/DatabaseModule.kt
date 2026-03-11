package com.nutrisport.database.di

import com.nutrisport.database.NutriSportDatabase
import org.koin.dsl.module

val databaseModule = module {
  single { get<NutriSportDatabase>().productDao() }
  single { get<NutriSportDatabase>().customerDao() }
  single { get<NutriSportDatabase>().cartItemDao() }
  single { get<NutriSportDatabase>().orderDao() }
}
