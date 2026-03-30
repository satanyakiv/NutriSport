package com.nutrisport.cart.component

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nutrisport.cart.model.CartItemUi
import com.nutrisport.shared.BorderIdle
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.component.QuantityCounter
import com.nutrisport.shared.domain.QuantityCounterSize
import org.jetbrains.compose.resources.painterResource

@Composable
fun CartItemCard(
  modifier: Modifier = Modifier,
  cartItemUi: CartItemUi,
  onMinusClick: (Int) -> Unit,
  onPlusClick: (Int) -> Unit,
  onDeleteClick: () -> Unit,
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(120.dp)
      .clip(RoundedCornerShape(size = 12.dp))
      .background(SurfaceLighter)
  ) {
    CartItemImage(thumbnail = cartItemUi.thumbnail)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(all = 12.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      CartItemHeader(
        title = cartItemUi.title,
        onDeleteClick = onDeleteClick,
      )
      CartItemPriceRow(
        formattedUnitPrice = cartItemUi.formattedUnitPrice,
        formattedPreviousUnitPrice = cartItemUi.formattedPreviousUnitPrice,
        quantity = cartItemUi.quantity,
        onMinusClick = onMinusClick,
        onPlusClick = onPlusClick,
      )
    }
  }
}

@Composable
private fun CartItemImage(thumbnail: String) {
  val context = LocalPlatformContext.current
  val imageRequest = remember(thumbnail) {
    ImageRequest.Builder(context)
      .data(thumbnail)
      .crossfade(enable = true)
      .build()
  }
  AsyncImage(
    modifier = Modifier
      .width(120.dp)
      .height(120.dp)
      .clip(RoundedCornerShape(size = 12.dp))
      .border(
        width = 1.dp,
        color = BorderIdle,
        shape = RoundedCornerShape(size = 12.dp)
      ),
    model = imageRequest,
    contentDescription = "Product thumbnail image",
    contentScale = ContentScale.Crop
  )
}

@Composable
private fun CartItemHeader(
  title: String,
  onDeleteClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = title,
      fontSize = FontSize.MEDIUM,
      color = TextPrimary,
      fontWeight = FontWeight.Medium,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.width(12.dp))
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(size = 6.dp))
        .background(Surface)
        .border(
          width = 1.dp,
          color = BorderIdle,
          shape = RoundedCornerShape(size = 6.dp)
        )
        .clickable { onDeleteClick() }
        .padding(all = 8.dp),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        modifier = Modifier.size(14.dp),
        painter = painterResource(Resources.Icon.Delete),
        contentDescription = "Delete icon",
        tint = IconPrimary
      )
    }
  }
}

@Composable
private fun CartItemPriceRow(
  formattedUnitPrice: String,
  formattedPreviousUnitPrice: String?,
  quantity: Int,
  onMinusClick: (Int) -> Unit,
  onPlusClick: (Int) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (formattedPreviousUnitPrice != null) {
        Text(
          text = formattedPreviousUnitPrice,
          fontSize = FontSize.SMALL,
          color = TextPrimary.copy(alpha = 0.5f),
          textDecoration = TextDecoration.LineThrough,
          maxLines = 1,
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = formattedUnitPrice,
        fontSize = FontSize.EXTRA_REGULAR,
        color = if (formattedPreviousUnitPrice != null) {
          SurfaceError
        } else {
          TextSecondary
        },
        fontWeight = FontWeight.Medium,
        maxLines = 1,
      )
    }
    QuantityCounter(
      size = QuantityCounterSize.Small,
      value = quantity,
      onMinusClick = onMinusClick,
      onPlusClick = onPlusClick,
    )
  }
}
