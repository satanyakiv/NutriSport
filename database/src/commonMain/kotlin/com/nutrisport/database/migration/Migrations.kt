package com.nutrisport.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      "ALTER TABLE products ADD COLUMN previouslyKnownPrice REAL DEFAULT NULL",
    )
  }
}
