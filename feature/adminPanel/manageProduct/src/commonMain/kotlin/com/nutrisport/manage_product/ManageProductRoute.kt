package com.nutrisport.manage_product

import androidx.compose.runtime.Composable
import com.nutrisport.manage_product.util.PhotoPicker
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ManageProductRoute(
  id: String?,
  goBack: () -> Unit,
) {
  val viewModel = koinViewModel<ManageProductViewModel>()
  val photoPicker = koinInject<PhotoPicker>()

  ManageProductScreen(
    id = id,
    goBack = goBack,
    screenState = viewModel.screenState,
    isFormValid = viewModel.isFormValid,
    thumbnailUploaderState = viewModel.thumbnailUploaderState,
    photoPicker = photoPicker,
    onTitleChange = viewModel::updateTitle,
    onDescriptionChange = viewModel::updateDescription,
    onCategoryChange = viewModel::updateCategory,
    onWeightChange = viewModel::updateWeight,
    onFlavorsChange = viewModel::updateFlavors,
    onPriceChange = viewModel::updatePrice,
    onIsNewChange = viewModel::updateIsNew,
    onIsPopularChange = viewModel::updateIsPopular,
    onIsDiscountedChange = viewModel::updateIsDiscounted,
    onUploadThumbnail = viewModel::uploadThumbnailToStorage,
    onDeleteThumbnail = viewModel::deleteThumbnail,
    onUpdateThumbnailState = viewModel::updateThumbnailUploaderState,
    onCreateProduct = viewModel::createNewProduct,
    onUpdateProduct = viewModel::updateProduct,
    onDeleteProduct = viewModel::deleteProduct,
  )
}
