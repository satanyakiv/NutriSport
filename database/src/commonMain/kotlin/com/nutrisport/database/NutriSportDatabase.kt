package com.nutrisport.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.nutrisport.database.converter.Converters
import com.nutrisport.database.dao.CartItemDao
import com.nutrisport.database.dao.CustomerDao
import com.nutrisport.database.dao.OrderDao
import com.nutrisport.database.dao.ProductDao
import com.nutrisport.database.entity.CartItemEntity
import com.nutrisport.database.entity.CustomerEntity
import com.nutrisport.database.entity.OrderEntity
import com.nutrisport.database.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
    ],
    version = 1,
)
@TypeConverters(Converters::class)
@ConstructedBy(NutriSportDatabaseConstructor::class)
abstract class NutriSportDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun orderDao(): OrderDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object NutriSportDatabaseConstructor : RoomDatabaseConstructor<NutriSportDatabase>
