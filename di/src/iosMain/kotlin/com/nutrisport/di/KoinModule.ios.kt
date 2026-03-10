package com.nutrisport.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.nutrisport.database.NutriSportDatabase
import com.nutrisport.database.getDatabaseBuilder
import com.nutrisport.manage_product.util.PhotoPicker
import org.koin.dsl.module

actual val targetModule = module {
  single { PhotoPicker() }
  single<NutriSportDatabase> {
    getDatabaseBuilder()
      .setDriver(BundledSQLiteDriver())
      .build()
  }
}
