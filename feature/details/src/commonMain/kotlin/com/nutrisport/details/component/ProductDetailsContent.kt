package com.nutrisport.details.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nutrisport.details.model.ProductUi
import com.nutrisport.shared.BorderIdle
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Resources
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.domain.ProductCategory
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ProductDetailsContent(product: ProductUi) {
  AsyncImage(
    modifier = Modifier
      .fillMaxWidth()
      .height(300.dp)
      .clip(RoundedCornerShape(size = 12.dp))
      .border(
        width = 1.dp,
        color = BorderIdle,
        shape = RoundedCornerShape(size = 12.dp),
      ),
    model = ImageRequest.Builder(LocalPlatformContext.current)
      .data(product.thumbnail)
      .crossfade(enable = true)
      .build(),
    contentDescription = "Product thumbnail image",
    contentScale = ContentScale.Crop,
  )
  Spacer(modifier = Modifier.height(12.dp))
  WeightAndPriceRow(product)
  Spacer(modifier = Modifier.height(12.dp))
  Text(
    text = product.title,
    fontSize = FontSize.EXTRA_MEDIUM,
    fontWeight = FontWeight.Medium,
    color = TextPrimary,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
  )
  Spacer(modifier = Modifier.height(12.dp))
  Text(
    text = product.description,
    fontSize = FontSize.REGULAR,
    lineHeight = FontSize.REGULAR * 1.3,
    color = TextPrimary,
  )
}

@Composable
private fun WeightAndPriceRow(product: ProductUi) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    AnimatedContent(targetState = product.category) { category ->
      if (ProductCategory.valueOf(category) == ProductCategory.Accessories) {
        Spacer(modifier = Modifier.weight(1f))
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            modifier = Modifier.size(14.dp),
            painter = painterResource(Resources.Icon.Weight),
            contentDescription = "Weight icon",
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = product.formattedWeight.orEmpty(),
            fontSize = FontSize.REGULAR,
            color = TextPrimary,
          )
        }
      }
    }
    Text(
      text = product.formattedPrice,
      fontSize = FontSize.MEDIUM,
      color = TextSecondary,
      fontWeight = FontWeight.Medium,
    )
  }
}
