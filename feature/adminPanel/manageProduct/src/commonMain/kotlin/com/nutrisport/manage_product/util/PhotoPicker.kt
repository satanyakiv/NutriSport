package com.nutrisport.manage_product.util

import androidx.compose.runtime.Composable
import dev.gitlive.firebase.storage.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhotoPicker {
  @Composable
  fun InitializePhotoPicker(onImageSelect: (File?) -> Unit)
  fun open()
}
