package com.nutrisport.manage_product.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nutrisport.shared.BorderIdle
import com.nutrisport.shared.ButtonPrimary
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.component.ErrorCard
import com.nutrisport.shared.component.LoadingCard
import com.nutrisport.shared.util.DisplayResult
import com.nutrisport.shared.util.UiState
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ThumbnailUploader(
  thumbnailUrl: String,
  thumbnailUploaderState: UiState<Unit>,
  onPickImage: () -> Unit,
  onDeleteThumbnail: () -> Unit,
  onResetState: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(300.dp)
      .clip(RoundedCornerShape(12.dp))
      .border(
        width = 1.dp,
        color = BorderIdle,
        shape = RoundedCornerShape(12.dp),
      )
      .background(SurfaceLighter)
      .clickable(enabled = thumbnailUploaderState.isIdle()) { onPickImage() },
    contentAlignment = Alignment.Center,
  ) {
    thumbnailUploaderState.DisplayResult(
      onIdle = { IdleContent() },
      onLoading = { LoadingCard(modifier = Modifier.fillMaxSize()) },
      onSuccess = {
        SuccessContent(
          thumbnailUrl = thumbnailUrl,
          onDeleteThumbnail = onDeleteThumbnail,
        )
      },
      onError = { ErrorContent(message = it, onRetry = onResetState) },
    )
  }
}

@Composable
private fun IdleContent() {
  Icon(
    painter = painterResource(Resources.Icon.Plus),
    contentDescription = "Plus icon",
    modifier = Modifier.size(24.dp),
    tint = IconPrimary,
  )
}

@Composable
private fun SuccessContent(
  thumbnailUrl: String,
  onDeleteThumbnail: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopEnd,
  ) {
    AsyncImage(
      modifier = Modifier.fillMaxSize(),
      model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(thumbnailUrl)
        .crossfade(enable = true)
        .build(),
      contentDescription = "Product thumbnail image",
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .padding(top = 12.dp, end = 12.dp)
        .background(ButtonPrimary)
        .clickable { onDeleteThumbnail() }
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
}

@Composable
private fun ErrorContent(
  message: String,
  onRetry: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    ErrorCard(message = message)
    Spacer(Modifier.height(12.dp))
    TextButton(
      onClick = onRetry,
      colors = ButtonDefaults.textButtonColors(
        containerColor = Transparent,
        contentColor = TextSecondary,
      ),
    ) {
      Text(
        fontSize = FontSize.SMALL,
        text = "Try again",
        color = TextSecondary,
      )
    }
  }
}
