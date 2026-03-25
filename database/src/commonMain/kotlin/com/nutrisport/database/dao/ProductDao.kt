package com.nutrisport.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.nutrisport.database.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

  @Query("SELECT * FROM products")
  fun observeAll(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isDiscounted = 1")
  fun observeDiscounted(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE isNew = 1")
  fun observeNew(): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id")
  fun observeById(id: String): Flow<ProductEntity?>

  @Query("SELECT * FROM products WHERE id IN (:ids)")
  fun observeByIds(ids: List<String>): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE category = :category")
  fun observeByCategory(category: String): Flow<List<ProductEntity>>

  @Query("SELECT * FROM products WHERE id = :id")
  suspend fun getById(id: String): ProductEntity?

  @Query("SELECT * FROM products WHERE id IN (:ids)")
  suspend fun getByIds(ids: List<String>): List<ProductEntity>

  @Upsert
  suspend fun upsertAll(products: List<ProductEntity>)

  @Query("UPDATE products SET previouslyKnownPrice = NULL WHERE id = :id")
  suspend fun clearPreviousPrice(id: String)

  @Query("DELETE FROM products")
  suspend fun deleteAll()
}
