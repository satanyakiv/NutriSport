package com.nutrisport.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<NutriSportDatabase> {
  val dbFile = context.getDatabasePath("nutrisport.db")
  return Room.databaseBuilder<NutriSportDatabase>(
    context = context,
    name = dbFile.absolutePath,
  )
}
