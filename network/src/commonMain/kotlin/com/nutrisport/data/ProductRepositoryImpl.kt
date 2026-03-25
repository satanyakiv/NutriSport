package com.nutrisport.data

import com.nutrisport.data.mapper.ProductDtoToEntityMapper
import com.nutrisport.data.mapper.ProductEntityToDomainMapper
import com.nutrisport.database.dao.ProductDao
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.DomainResult
import com.nutrisport.shared.util.Either
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Query
import dev.gitlive.firebase.firestore.firestore
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ProductRepositoryImpl(
  private val firebaseMapper: ProductMapper,
  private val dtoToEntity: ProductDtoToEntityMapper,
  private val entityToDomain: ProductEntityToDomainMapper,
  private val productDao: ProductDao,
) : ProductRepository {
  private val productCollection = Firebase.firestore.collection(collectionPath = COLLECTION_NAME)
  private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  companion object {
    private const val COLLECTION_NAME = "product"
  }

  override fun getCurrentUserId(): String? = currentUserId()

  override fun readDiscountedProducts(): Flow<DomainResult<List<Product>>> {
    syncFromFirebase { productCollection.where { "isDiscounted" equalTo true } }
    return productDao.observeDiscounted()
      .map<_, DomainResult<List<Product>>> { Either.Right(entityToDomain.map(it)) }
      .catch { emit(Either.Left(AppError.Network("Error while retrieving products: ${it.message}"))) }
  }

  override fun readNewProducts(): Flow<DomainResult<List<Product>>> {
    syncFromFirebase { productCollection.where { "isNew" equalTo true } }
    return productDao.observeNew()
      .map<_, DomainResult<List<Product>>> { Either.Right(entityToDomain.map(it)) }
      .catch { emit(Either.Left(AppError.Network("Error while retrieving products: ${it.message}"))) }
  }

  override fun readProductByIdFlow(id: String): Flow<DomainResult<Product>> {
    if (currentUserId() == null) return flowOf(Either.Left(AppError.Unauthorized()))
    syncSingleFromFirebase(id)
    return productDao.observeById(id)
      .map<_, DomainResult<Product>> { entity ->
        if (entity != null) {
          Either.Right(entityToDomain.map(entity))
        } else {
          Either.Left(AppError.NotFound("Product $id not found"))
        }
      }
      .catch { emit(Either.Left(AppError.Network("Error while reading selected product: ${it.message}"))) }
  }

  override fun readProductsByIdsFlow(ids: List<String>): Flow<DomainResult<List<Product>>> {
    if (currentUserId() == null) return flowOf(Either.Left(AppError.Unauthorized()))
    if (ids.isEmpty()) return flowOf(Either.Right(emptyList()))

    syncByIdsFromFirebase(ids)
    return productDao.observeByIds(ids)
      .map<_, DomainResult<List<Product>>> { Either.Right(entityToDomain.map(it)) }
      .catch { emit(Either.Left(AppError.Network("Error while reading selected products: ${it.message}"))) }
  }

  override fun readProductsByCategoryFlow(
    category: ProductCategory,
  ): Flow<DomainResult<List<Product>>> {
    syncFromFirebase { productCollection.where { "category" equalTo category.name } }
    return productDao.observeByCategory(category.name)
      .map<_, DomainResult<List<Product>>> { Either.Right(entityToDomain.map(it)) }
      .catch { emit(Either.Left(AppError.Network("Error while reading products: ${it.message}"))) }
  }

  override suspend fun refreshProductById(id: String): DomainResult<Product> {
    return try {
      val document = productCollection.document(id).get()
      if (document.exists) {
        val dto = firebaseMapper.map(document)
        val currentEntity = productDao.getById(id)
        val entity = dtoToEntity.map(dto, currentEntity)
        productDao.upsertAll(listOf(entity))
        Either.Right(entityToDomain.map(entity))
      } else {
        Either.Left(AppError.NotFound("Product $id not found"))
      }
    } catch (e: Exception) {
      Napier.e("Error refreshing product $id: ${e.message}")
      Either.Left(AppError.Network("Failed to refresh product: ${e.message}"))
    }
  }

  override suspend fun acknowledgePriceChange(productId: String) {
    productDao.clearPreviousPrice(productId)
  }

  private fun syncFromFirebase(query: () -> Query) {
    if (currentUserId() == null) return
    syncScope.launch {
      try {
        query().snapshots.collect { snapshot ->
          val dtos = snapshot.documents.map { firebaseMapper.map(it) }
          val ids = dtos.map { it.id }
          val currentEntities = productDao.getByIds(ids).associateBy { it.id }
          val entities = dtos.map { dto ->
            dtoToEntity.map(dto, currentEntities[dto.id])
          }
          productDao.upsertAll(entities)
        }
      } catch (e: Exception) {
        Napier.e("Firebase sync error: ${e.message}")
      }
    }
  }

  private fun syncSingleFromFirebase(id: String) {
    if (currentUserId() == null) return
    syncScope.launch {
      try {
        productCollection.document(id).snapshots.collect { document ->
          if (document.exists) {
            val dto = firebaseMapper.map(document)
            val currentEntity = productDao.getById(id)
            productDao.upsertAll(listOf(dtoToEntity.map(dto, currentEntity)))
          }
        }
      } catch (e: Exception) {
        Napier.e("Firebase sync error: ${e.message}")
      }
    }
  }

  private fun syncByIdsFromFirebase(ids: List<String>) {
    if (currentUserId() == null) return
    syncScope.launch {
      try {
        val chunkFlows = ids.chunked(10).map { chunk ->
          productCollection
            .where { "id" inArray chunk }
            .snapshots
            .map { query -> query.documents.map { firebaseMapper.map(it) } }
        }
        combine(chunkFlows) { arrays -> arrays.toList().flatten() }
          .collect { dtos ->
            val dtoIds = dtos.map { it.id }
            val currentEntities = productDao.getByIds(dtoIds).associateBy { it.id }
            val entities = dtos.map { dto ->
              dtoToEntity.map(dto, currentEntities[dto.id])
            }
            productDao.upsertAll(entities)
          }
      } catch (e: Exception) {
        Napier.e("Firebase sync error: ${e.message}")
      }
    }
  }
}
