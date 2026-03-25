package com.nutrisport.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.nutrisport.data.connectivity.AndroidConnectivityObserver
import com.nutrisport.database.NutriSportDatabase
import com.nutrisport.database.getDatabaseBuilder
import com.nutrisport.database.migration.MIGRATION_1_2
import com.nutrisport.manage_product.util.PhotoPicker
import com.nutrisport.shared.domain.ConnectivityObserver
import org.koin.dsl.module

actual val targetModule = module {
  single { PhotoPicker() }
  single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
  single<NutriSportDatabase> {
    getDatabaseBuilder(get())
      .addMigrations(MIGRATION_1_2)
      .setDriver(BundledSQLiteDriver())
      .build()
  }
}
