package com.nutrisport.manage_product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.AppError
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.launch

class ManageProductViewModel(
  private val adminRepository: AdminRepository,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val productId = savedStateHandle.get<String>("id").orEmpty()

  var screenState by mutableStateOf(ManageProductState())
    private set
  val isFormValid: Boolean
    get() = screenState.title.isNotBlank()
        && screenState.description.isNotBlank()
        && screenState.thumbnail.isNotEmpty()
        && screenState.price != 0.0

  var thumbnailUploaderState: UiState<Unit> by mutableStateOf(UiState.Idle)
    private set

  init {
    productId.takeIf { it.isNotEmpty() }?.let {
      viewModelScope.launch {
        adminRepository.readProductById(productId).fold(
          ifLeft = { /* ignore load error for edit screen */ },
          ifRight = { product ->
            screenState = ManageProductState(
              id = product.id,
              createdAt = product.createdAt,
              title = product.title,
              description = product.description,
              thumbnail = product.thumbnail,
              category = ProductCategory.valueOf(product.category),
              flavors = product.flavors?.joinToString(",").orEmpty(),
              weight = product.weight,
              price = product.price,
              isNew = product.isNew,
              isPopular = product.isPopular,
              isDiscounted = product.isDiscounted,
            )
            thumbnailUploaderState = UiState.Content(Either.Right(Unit))
          },
        )
      }
    }
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

  fun updateThumbnailUploaderState(value: UiState<Unit>) {
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
      adminRepository.createNewProduct(screenState.toProduct()).fold(
        ifLeft = { error -> onError(error.message) },
        ifRight = { onSuccess() },
      )
    }
  }

  fun uploadThumbnailToStorage(file: File?, onSuccess: () -> Unit) {
    if (file == null) {
      thumbnailUploaderState = UiState.Content(
        Either.Left(AppError.Unknown("File is null. Error while selecting an image")),
      )
      return
    }
    viewModelScope.launch {
      thumbnailUploaderState = UiState.Loading
      adminRepository.uploadImageToStorage(file).fold(
        ifLeft = { error -> setThumbnailError(error.message) },
        ifRight = { url -> syncThumbnail(url, UiState.Content(Either.Right(Unit)), onSuccess) },
      )
    }
  }

  fun updateProduct(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    if (isFormValid) {
      viewModelScope.launch {
        adminRepository.updateProduct(screenState.toProduct()).fold(
          ifLeft = { error -> onError(error.message) },
          ifRight = { onSuccess() },
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
      adminRepository.deleteImageFromStorage(screenState.thumbnail).fold(
        ifLeft = { error -> onError(error.message) },
        ifRight = { syncThumbnail("", UiState.Idle, onSuccess, onError) },
      )
    }
  }

  fun deleteProduct(
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
  ) {
    productId.takeIf { it.isNotEmpty() }?.let { _ ->
      viewModelScope.launch {
        adminRepository.deleteProduct(
          productId = productId,
        ).fold(
          ifLeft = { error -> onError(error.message) },
          ifRight = {
            deleteThumbnail(
              onSuccess = { },
              onError = { },
            )
            onSuccess()
          }
        )
      }
    }
  }

  fun updateIsNew(new: Boolean) {
    screenState = screenState.copy(isNew = new)
  }

  fun updateIsPopular(it: Boolean) {
    screenState = screenState.copy(isPopular = it)
  }

  fun updateIsDiscounted(it: Boolean) {
    screenState = screenState.copy(isDiscounted = it)
  }

  private suspend fun syncThumbnail(
    url: String,
    successState: UiState<Unit>,
    onSuccess: () -> Unit,
    onError: ((String) -> Unit)? = null,
  ) {
    if (productId.isNotEmpty()) {
      adminRepository.updateProductThumbnail(productId, url).fold(
        ifLeft = { error -> onError?.invoke(error.message) ?: setThumbnailError(error.message) },
        ifRight = { applyThumbnail(url, successState, onSuccess) },
      )
    } else {
      applyThumbnail(url, successState, onSuccess)
    }
  }

  private fun applyThumbnail(url: String, state: UiState<Unit>, onSuccess: () -> Unit) {
    screenState = screenState.copy(thumbnail = url)
    thumbnailUploaderState = state
    onSuccess()
  }

  private fun setThumbnailError(message: String) {
    thumbnailUploaderState = UiState.Content(Either.Left(AppError.Unknown(message)))
  }
}
