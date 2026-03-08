package com.nutrisport.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val title: String,
    val description: String,
    val thumbnail: String,
    val category: String,
    val flavors: String?,
    val weight: Int?,
    val price: Double,
    val isPopular: Boolean,
    val isDiscounted: Boolean,
    val isNew: Boolean,
)
