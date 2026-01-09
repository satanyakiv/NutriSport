package com.nutrisport.manage_product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ManageProductState(
  val id: String = Uuid.random().toHexString(),
  val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
  val title: String = "",
  val description: String = "",
  val thumbnail: String = "",
  val category: ProductCategory = ProductCategory.Protein,
  val flavors: String? = null,
  val weight: Int? = null,
  val price: Double = 0.0,
)

class ManageProductViewModule(
  private val adminRepository: AdminRepository,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val productId = savedStateHandle.get<String>("id") ?: ""

  var screenState by mutableStateOf(ManageProductState())
    private set
  val isFormValid: Boolean
    get() = screenState.title.isNotBlank()
        && screenState.description.isNotBlank()
        && screenState.thumbnail.isNotEmpty()
        && screenState.price != 0.0

  var thumbnailUploaderState: RequestState<Unit> by mutableStateOf(RequestState.Idle)
    private set

  init {
    productId.takeIf { it.isNotEmpty() }?.let {
      viewModelScope.launch {
        val selectedProduct = adminRepository.readProductById(productId)
        if (selectedProduct.isSuccess()) {
          val product = selectedProduct.getSuccessData()
          updateId(product.id)
          updateCreatedAt(product.createdAt)
          updateTitle(product.title)
          updateDescription(product.description)
          updateThumbnail(product.thumbnail)
          updateThumbnailUploaderState(RequestState.Success(Unit))
          updateCategory(ProductCategory.valueOf(product.category))
          updateFlavors(product.flavors?.joinToString(",").orEmpty())
          updateWeight(product.weight)
          updatePrice(product.price)
        }
      }
    }
  }

  fun updateId(id: String) {
    screenState = screenState.copy(id = id)
  }

  fun updateCreatedAt(createdAt: Long) {
    screenState = screenState.copy(createdAt = createdAt)
  }

  fun updateTitle(title: String) {
    screenState = screenState.copy(title = title)
  }

  fun updateDescription(description: String) {
    screenState = screenState.copy(description = description)
  }

  fun updateThumbnail(thumbnail: String) {
    screenState = screenState.copy(thumbnail = thumbnail)
  }

  fun updateThumbnailUploaderState(value: RequestState<Unit>) {
    thumbnailUploaderState = value
  }

  fun updateCategory(category: ProductCategory) {
    screenState = screenState.copy(category = category)
  }

  fun updateFlavors(flavors: String?) {
    screenState = screenState.copy(flavors = flavors)
  }

  fun updateWeight(weight: Int?) {
    screenState = screenState.copy(weight = weight)
  }

  fun updatePrice(price: Double) {
    screenState = screenState.copy(price = price)
  }

  fun createNewProduct(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      adminRepository.createNewProduct(
        product = Product(
          id = screenState.id,
          title = screenState.title,
          description = screenState.description,
          thumbnail = screenState.thumbnail,
          category = screenState.category.title,
          flavors = screenState.flavors?.split(","),
          weight = screenState.weight,
          price = screenState.price,
        ),
        onSuccess = onSuccess,
        onError = onError,
      )
    }
  }

  fun uploadThumbnailToStorage(
    file: File?,
    onSuccess: () -> Unit
  ) {
    if (file == null) {
      updateThumbnailUploaderState(RequestState.Error("File is null. Error while selecting an image"))
      return
    }

    viewModelScope.launch {
      updateThumbnailUploaderState(RequestState.Loading)
      val downloadUrl = adminRepository.uploadImageToStorage(file)
      try {
        if (downloadUrl.isNullOrEmpty()) {
          throw Exception("Error while uploading image to storage")
        }

        productId.takeIf { it.isNotEmpty() }?.let { id ->
          adminRepository.updateImageThumbnail(
            productId = productId,
            downloadUrl = downloadUrl,
            onSuccess = {
              updateThumbnail(downloadUrl)
              updateThumbnailUploaderState(RequestState.Success(Unit))
              onSuccess()
            },
            onError = {
              updateThumbnailUploaderState(RequestState.Error(it))
            }
          )
        } ?: run {
          updateThumbnail(downloadUrl)
          updateThumbnailUploaderState(RequestState.Success(Unit))
          onSuccess()
        }
      } catch (e: Exception) {
        updateThumbnailUploaderState(RequestState.Error(e.message.orEmpty()))
      }
    }
  }

  fun updateProduct(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    if (isFormValid) {
      viewModelScope.launch {
        adminRepository.updateProduct(
          product = Product(
            id = screenState.id,
            createdAt = screenState.createdAt,
            title = screenState.title,
            description = screenState.description,
            thumbnail = screenState.thumbnail,
            category = screenState.category.name,
            flavors = screenState.flavors
              ?.split(",")
              ?.map { it.trim() }
              ?.filter { it.isNotEmpty() },
            weight = screenState.weight,
            price = screenState.price,
          ),
          onSuccess = onSuccess,
          onError = onError,
        )
      }
    } else {
      onError("Please fill in the form correctly")
    }
  }

  fun deleteThumbnail(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    viewModelScope.launch {
      adminRepository.deleteImageFromStorage(
        downloadUrl = screenState.thumbnail,
        onSuccess = {
          productId.takeIf { it.isNotEmpty() }?.let { id ->
            viewModelScope.launch {
              adminRepository.updateImageThumbnail(
                productId = productId,
                downloadUrl = "",
                onSuccess = {
                  updateThumbnail("")
                  updateThumbnailUploaderState(RequestState.Idle)
                  onSuccess()
                },
                onError = onError,
              )
            }
          } ?: run {
            updateThumbnail("")
            updateThumbnailUploaderState(RequestState.Idle)
            onSuccess()
          }
          updateThumbnail("")
          updateThumbnailUploaderState(RequestState.Idle)
          onSuccess()
        },
        onError = onError,
      )
    }
  }

  fun deleteProduct(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    productId.takeIf { it.isNotEmpty() }?.let { id ->
      viewModelScope.launch {
        adminRepository.deleteProduct(
          productId = productId,
          onSuccess = {
            deleteThumbnail(
              onSuccess = { },
              onError = { },
            )
            onSuccess()
          },
          onError = onError,
        )
      }
    }
  }
}
