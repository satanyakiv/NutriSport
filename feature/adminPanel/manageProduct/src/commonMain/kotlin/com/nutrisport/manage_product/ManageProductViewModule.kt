package com.nutrisport.manage_product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.shared.domain.Product
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.RequestState
import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ManageProductState(
  val id: String = Uuid.random().toHexString(),
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
) : ViewModel() {
  var screenState by mutableStateOf(ManageProductState())
    private set
  val isFormValid: Boolean
    get() = screenState.title.isNotBlank()
        && screenState.description.isNotBlank()
        && screenState.thumbnail.isNotEmpty()
        && screenState.price != 0.0

  var thumbnailUploaderState: RequestState<Unit> by mutableStateOf(RequestState.Idle)
    private set

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

  fun updateWeight(weight: Int) {
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
        } else {
          updateThumbnail(downloadUrl)
          updateThumbnailUploaderState(RequestState.Success(Unit))
          onSuccess()
        }
      } catch (e: Exception) {
        updateThumbnailUploaderState(RequestState.Error(e.message.orEmpty()))
      }
    }
  }
}
