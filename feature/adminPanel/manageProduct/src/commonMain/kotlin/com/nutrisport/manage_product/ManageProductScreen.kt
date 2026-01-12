package com.nutrisport.manage_product

import ContentWithMessageBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nutrisport.manage_product.util.PhotoPicker
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.BorderIdle
import com.nutrisport.shared.ButtonPrimary
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceDarker
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.SurfaceSecondary
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.component.AlertTextField
import com.nutrisport.shared.component.CustomTextField
import com.nutrisport.shared.component.ErrorCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.component.PrimaryButton
import com.nutrisport.shared.component.dialog.CategoriesDialog
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.RequestState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import rememberMessageBarState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageProductScreen(
  id: String?,
  goBack: () -> Unit,
) {
  val messageBarState = rememberMessageBarState()
  var category by remember { mutableStateOf(ProductCategory.Protein) }
  var showCategoriesDialog by remember { mutableStateOf(false) }
  var dropDownMenuOpen by remember { mutableStateOf(false) }
  val viewModel = koinViewModel<ManageProductViewModule>()
  val screenState = viewModel.screenState
  val isFormValid = viewModel.isFormValid
  val thumbnailUploaderState = viewModel.thumbnailUploaderState
  val photoPicker = koinInject<PhotoPicker>()

  photoPicker.InitializePhotoPicker { file ->
    viewModel.uploadThumbnailToStorage(
      file = file,
      onSuccess = {
        messageBarState.addSuccess("Thumbnail successfully uploaded")
      },
    )
  }

  AnimatedVisibility(
    visible = showCategoriesDialog
  ) {
    CategoriesDialog(
      category = screenState.category,
      onDismiss = { showCategoriesDialog = false },
      onConfirmClick = {
        viewModel.updateCategory(it)
        showCategoriesDialog = false
      }
    )
  }

  Scaffold(
    containerColor = Surface,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (id == null) "Add Product" else "Edit Product",
            fontFamily = BebasNeueFont(),
            fontSize = FontSize.LARGE,
            color = TextPrimary
          )
        },
        navigationIcon = {
          IconButton(onClick = goBack) {
            Icon(
              painter = painterResource(Resources.Icon.BackArrow),
              contentDescription = "Back Arrow icon",
              tint = IconPrimary
            )
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = Surface,
          scrolledContainerColor = Surface,
          navigationIconContentColor = IconPrimary,
          titleContentColor = TextPrimary,
          actionIconContentColor = IconPrimary
        ),
        actions = {
          id?.let {
            Box {
              IconButton(onClick = { dropDownMenuOpen = true }) {
                Icon(
                  painter = painterResource(Resources.Icon.VerticalMenu),
                  contentDescription = "Vertical menu icon",
                  tint = IconPrimary
                )
              }
              DropdownMenu(
                expanded = dropDownMenuOpen,
                onDismissRequest = { dropDownMenuOpen = false }
              ) {
                DropdownMenuItem(
                  text = { Text("Delete", color = TextPrimary) },
                  leadingIcon = {
                    Icon(
                      modifier = Modifier.size(14.dp),
                      painter = painterResource(Resources.Icon.Delete),
                      contentDescription = "Delete icon",
                      tint = IconPrimary,
                    )
                  },
                  onClick = {
                    dropDownMenuOpen = false
                    viewModel.deleteProduct(
                      onSuccess = goBack,
                      onError = { messageBarState.addError(it) },
                    )
                  },
                )
              }
            }
          }
        }
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
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(300.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(
                width = 1.dp,
                color = BorderIdle,
                shape = RoundedCornerShape(12.dp)
              )
              .background(SurfaceLighter)
              .clickable(
                enabled = thumbnailUploaderState.isIdle(),
              ) {
                photoPicker.open()
              },
            contentAlignment = Alignment.Center,
          ) {
            thumbnailUploaderState.DisplayResult(
              onIdle = {
                Icon(
                  painter = painterResource(Resources.Icon.Plus),
                  contentDescription = "Plus icon",
                  modifier = Modifier.size(24.dp),
                  tint = IconPrimary
                )
              },
              onLoading = {
                LoadingCard(
                  modifier = Modifier.fillMaxSize()
                )
              },
              onSuccess = {
                Box(
                  modifier = Modifier.fillMaxSize(),
                  contentAlignment = Alignment.TopEnd,
                ) {
                  AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ImageRequest.Builder(LocalPlatformContext.current).data(screenState.thumbnail).crossfade(enable = true).build(),
                    contentDescription = "Product thumbnail image",
                    contentScale = ContentScale.Crop,
                  )
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .padding(top = 12.dp, end = 12.dp)
                      .background(ButtonPrimary)
                      .clickable {
                        viewModel.deleteThumbnail(
                          onSuccess = {
                            messageBarState.addSuccess("Thumbnail successfully deleted")
                          },
                          onError = {
                            messageBarState.addError("Error while deleting thumbnail: $it")
                          }
                        )
                      }
                      .padding(12.dp),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      modifier = Modifier.size(14.dp),
                      painter = painterResource(Resources.Icon.Delete),
                      contentDescription = "Delete icon",
                    )
                  }
                }
              },
              onError = {
                Column(
                  modifier = Modifier.fillMaxSize(),
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  ErrorCard(message = it)
                  Spacer(Modifier.height(12.dp))
                  TextButton(
                    onClick = {
                      viewModel.updateThumbnailUploaderState(RequestState.Idle)
                    },
                    colors = ButtonDefaults.textButtonColors(
                      containerColor = Transparent,
                      contentColor = TextSecondary,
                    )
                  ) {
                    Text(
                      fontSize = FontSize.SMALL,
                      text = "Try again",
                      color = TextSecondary,
                    )
                  }
                }
              },
            )
          }
          CustomTextField(
            value = screenState.title,
            onValueChange = viewModel::updateTitle,
            placeholder = "Title",
          )
          CustomTextField(
            modifier = Modifier.height(120.dp),
            value = screenState.description,
            onValueChange = viewModel::updateDescription,
            placeholder = "Description",
            expanded = true,
          )
          AlertTextField(
            modifier = Modifier.fillMaxWidth(),
            text = screenState.category.title,
            onClick = { showCategoriesDialog = true },
          )
          AnimatedVisibility(
            visible = screenState.category != ProductCategory.Accessories,
          ) {
            Column {
              CustomTextField(
                value = screenState.weight?.toString().orEmpty(),
                onValueChange = { viewModel.updateWeight(it.toIntOrNull() ?: 0) },
                placeholder = "Weight",
                keyboardOptions = KeyboardOptions(
                  keyboardType = KeyboardType.Number,
                )
              )
              Spacer(Modifier.height(12.dp))
              CustomTextField(
//            value = screenState.flavors?.joinToString { ", " }.orEmpty(),
                value = screenState.flavors.orEmpty(),
                onValueChange = {
//              val flavors = it.split(",").map { flavor -> flavor.trim() }
                  viewModel.updateFlavors(it)
                },
                placeholder = "Flavors",
              )
            }
          }
          CustomTextField(
            value = screenState.price.toString(),
            onValueChange = {
              if (it.toDoubleOrNull() != null) {
                viewModel.updatePrice(it.toDoubleOrNull() ?: 0.0)
              }
            },
            placeholder = "Price",
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
            )
          )
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
          ) {
            Row (
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "New",
                fontSize = FontSize.REGULAR,
                color = TextPrimary,
                modifier = Modifier.padding(start = 12.dp)
              )
              Switch(
                checked = screenState.isNew,
                onCheckedChange = { viewModel.updateIsNew(it) },
                colors = SwitchDefaults.colors(
                  checkedTrackColor = SurfaceSecondary,
                  uncheckedTrackColor = SurfaceDarker,
                  checkedThumbColor = Surface,
                  uncheckedThumbColor = Surface,
                  checkedBorderColor = SurfaceSecondary,
                  uncheckedBorderColor = SurfaceDarker,
                )
              )
            }
            Row (
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                modifier = Modifier.padding(start = 12.dp),
                text = "Popular",
                fontSize = FontSize.REGULAR,
                color = TextPrimary,
              )
              Switch(
                checked = screenState.isPopular,
                onCheckedChange = { viewModel.updateIsPopular(it) },
                colors = SwitchDefaults.colors(
                  checkedTrackColor = SurfaceSecondary,
                  uncheckedTrackColor = SurfaceDarker,
                  checkedThumbColor = Surface,
                  uncheckedThumbColor = Surface,
                  checkedBorderColor = SurfaceSecondary,
                  uncheckedBorderColor = SurfaceDarker,
                )
              )
            }
            Row (
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                modifier = Modifier.padding(start = 12.dp),
                text = "Discounted",
                fontSize = FontSize.REGULAR,
                color = TextPrimary,
              )
              Switch(
                checked = screenState.isDiscounted,
                onCheckedChange = { viewModel.updateIsDiscounted(it) },
                colors = SwitchDefaults.colors(
                  checkedTrackColor = SurfaceSecondary,
                  uncheckedTrackColor = SurfaceDarker,
                  checkedThumbColor = Surface,
                  uncheckedThumbColor = Surface,
                  checkedBorderColor = SurfaceSecondary,
                  uncheckedBorderColor = SurfaceDarker,
                )
              )
            }
          }
          Spacer(Modifier.height(24.dp))
        }
        PrimaryButton(
          text = if (id == null) "Add new Product" else "Update",
          icon = if (id == null) Resources.Icon.Plus else Resources.Icon.Checkmark,
          onClick = {
            if (id == null) {
              viewModel.createNewProduct(
                onSuccess = { messageBarState.addSuccess("Product successfully added!") },
                onError = { messageBarState.addError(it) }
              )
            } else {
              viewModel.updateProduct(
                onSuccess = { messageBarState.addSuccess("Product successfully updated!") },
                onError = { messageBarState.addError(it) }
              )
            }
          },
          enabled = isFormValid,
        )
      }
    }
  }
}
