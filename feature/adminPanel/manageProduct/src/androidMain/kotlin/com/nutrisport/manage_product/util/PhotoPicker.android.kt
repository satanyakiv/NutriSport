package com.nutrisport.manage_product.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.gitlive.firebase.storage.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PhotoPicker {
  private var openPhotoPicker by mutableStateOf(false)

  @MainThread
  actual fun open() {
    openPhotoPicker = true
  }

  @Composable
  actual fun InitializePhotoPicker(
    onImageSelect: (File?) -> Unit,
  ) {
    val pickMedia = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
      uri?.let { onImageSelect(File(it)) } ?: onImageSelect(null)
      openPhotoPicker = false
    }

    LaunchedEffect(openPhotoPicker) {
      if (openPhotoPicker) {
        openPhotoPicker = false
        pickMedia.launch(
          PickVisualMediaRequest(
            ActivityResultContracts.PickVisualMedia.ImageOnly
          )
        )
      }
    }
  }
}