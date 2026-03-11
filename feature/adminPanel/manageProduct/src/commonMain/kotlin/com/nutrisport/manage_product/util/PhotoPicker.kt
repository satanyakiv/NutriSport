package com.nutrisport.manage_product.util

import androidx.compose.runtime.Composable
import com.nutrisport.shared.domain.PlatformFile

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhotoPicker {
  @Composable
  fun InitializePhotoPicker(onImageSelect: (PlatformFile?) -> Unit)
  fun open()
}
