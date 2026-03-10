package com.nutrisport.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getDatabaseBuilder(): RoomDatabase.Builder<NutriSportDatabase> {
  val dbFilePath = NSHomeDirectory() + "/Documents/nutrisport.db"
  return Room.databaseBuilder<NutriSportDatabase>(
    name = dbFilePath,
  )
}
