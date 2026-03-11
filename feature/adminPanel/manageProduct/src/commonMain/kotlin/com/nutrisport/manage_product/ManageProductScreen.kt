package com.nutrisport.manage_product

import ContentWithMessageBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.manage_product.component.ManageProductTopBar
import com.nutrisport.manage_product.component.ProductFormFields
import com.nutrisport.manage_product.component.ThumbnailUploader
import com.nutrisport.manage_product.util.PhotoPicker
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.PrimaryButton
import com.nutrisport.shared.component.dialog.CategoriesDialog
import com.nutrisport.shared.domain.PlatformFile
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.UiState
import rememberMessageBarState

@Composable
fun ManageProductScreen(
  id: String?,
  goBack: () -> Unit,
  screenState: ManageProductState,
  isFormValid: Boolean,
  thumbnailUploaderState: UiState<Unit>,
  photoPicker: PhotoPicker,
  onTitleChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onCategoryChange: (ProductCategory) -> Unit,
  onWeightChange: (Int?) -> Unit,
  onFlavorsChange: (String?) -> Unit,
  onPriceChange: (Double) -> Unit,
  onIsNewChange: (Boolean) -> Unit,
  onIsPopularChange: (Boolean) -> Unit,
  onIsDiscountedChange: (Boolean) -> Unit,
  onUploadThumbnail: (PlatformFile?, () -> Unit) -> Unit,
  onDeleteThumbnail: (() -> Unit, (String) -> Unit) -> Unit,
  onUpdateThumbnailState: (UiState<Unit>) -> Unit,
  onCreateProduct: (() -> Unit, (String) -> Unit) -> Unit,
  onUpdateProduct: (() -> Unit, (String) -> Unit) -> Unit,
  onDeleteProduct: (() -> Unit, (String) -> Unit) -> Unit,
) {
  val messageBarState = rememberMessageBarState()
  var showCategoriesDialog by remember { mutableStateOf(false) }

  photoPicker.InitializePhotoPicker { file ->
    onUploadThumbnail(
      file,
      { messageBarState.addSuccess("Thumbnail successfully uploaded") },
    )
  }

  AnimatedVisibility(
    visible = showCategoriesDialog
  ) {
    CategoriesDialog(
      category = screenState.category,
      onDismiss = { showCategoriesDialog = false },
      onConfirmClick = {
        onCategoryChange(it)
        showCategoriesDialog = false
      }
    )
  }

  Scaffold(
    containerColor = Surface,
    topBar = {
      ManageProductTopBar(
        isEditMode = id != null,
        onBack = goBack,
        onDelete = {
          onDeleteProduct(
            goBack,
            { messageBarState.addError(it) },
          )
        },
      )
    },
  ) { padding ->
    ContentWithMessageBar(
      contentBackgroundColor = Surface,
      modifier = Modifier
        .padding(
          top = padding.calculateTopPadding(),
          bottom = padding.calculateBottomPadding()
        ),
      messageBarState = messageBarState,
      errorMaxLines = 2,
      errorContainerColor = SurfaceError,
      errorContentColor = TextWhite,
      successContainerColor = SurfaceBrand,
      successContentColor = TextPrimary
    ) {
      Column(
        modifier = Modifier
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp, top = 12.dp)
          .imePadding()
      ) {
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          ThumbnailUploader(
            thumbnailUrl = screenState.thumbnail,
            thumbnailUploaderState = thumbnailUploaderState,
            onPickImage = { photoPicker.open() },
            onDeleteThumbnail = {
              onDeleteThumbnail(
                { messageBarState.addSuccess("Thumbnail successfully deleted") },
                { messageBarState.addError("Error while deleting thumbnail: $it") },
              )
            },
            onResetState = { onUpdateThumbnailState(UiState.Idle) },
          )
          ProductFormFields(
            screenState = screenState,
            onTitleChange = onTitleChange,
            onDescriptionChange = onDescriptionChange,
            onCategoryClick = { showCategoriesDialog = true },
            onWeightChange = onWeightChange,
            onFlavorsChange = onFlavorsChange,
            onPriceChange = onPriceChange,
            onIsNewChange = onIsNewChange,
            onIsPopularChange = onIsPopularChange,
            onIsDiscountedChange = onIsDiscountedChange,
          )
          Spacer(Modifier.height(24.dp))
        }
        PrimaryButton(
          text = if (id == null) "Add new Product" else "Update",
          icon = if (id == null) Resources.Icon.Plus else Resources.Icon.Checkmark,
          onClick = {
            if (id == null) {
              onCreateProduct(
                { messageBarState.addSuccess("Product successfully added!") },
                { messageBarState.addError(it) },
              )
            } else {
              onUpdateProduct(
                { messageBarState.addSuccess("Product successfully updated!") },
                { messageBarState.addError(it) },
              )
            }
          },
          enabled = isFormValid,
        )
      }
    }
  }
}
