package com.nutrisport.products_overview.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Resources
import com.nutrisport.shared.TextBrand
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite
import com.nutrisport.shared.domain.Product
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainProductCard(
  modifier: Modifier = Modifier,
  product: Product,
  isVisible: Boolean = false,
  onClick: (String) -> Unit,
) {
  val infiniteTransition = rememberInfiniteTransition()
  val animatedScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(10000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )
  val animatedRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 10f,
    animationSpec = infiniteRepeatable(
      animation = tween(10000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
  )

  Box(
    modifier = modifier
      .fillMaxHeight()
      .clickable { onClick(product.id) },
  ) {
    AsyncImage(
      modifier = Modifier.fillMaxSize()
        .clip(RoundedCornerShape(12.dp))
        .animateContentSize()
        .then(if (isVisible) Modifier.scale(animatedScale).rotate(animatedRotation) else Modifier),
      model = ImageRequest.Builder(LocalPlatformContext.current).data(product.thumbnail).crossfade(true).build(),
      contentDescription = "Image of ${product.title}",
      contentScale = ContentScale.Crop,
    )
    Box(
      modifier = Modifier.fillMaxSize()
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(Color.Black, Color.Black.copy(alpha = Alpha.ZERO)),
            startY = Float.POSITIVE_INFINITY,
            endY = 0f,
          ),
        )
    )
    Column(
      modifier = Modifier.fillMaxSize()
        .padding(12.dp),
      verticalArrangement = Arrangement.Bottom,
    ) {
      Text(
        text = product.title,
        fontSize = FontSize.EXTRA_MEDIUM,
        color = TextWhite,
        maxLines = 2,
        fontWeight = FontWeight.Medium,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(Modifier.size(4.dp))
      Text(
        text = product.description,
        fontSize = FontSize.REGULAR,
        color = TextWhite.copy(alpha = Alpha.HALF),
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(Modifier.size(12.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          AnimatedContent(
            targetState = product.category,
          ) {
            product.weight?.let {
              Row(
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  painter = painterResource(Resources.Icon.Weight),
                  contentDescription = "Weight icon",
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                  text = "${product.weight}g",
                  fontSize = FontSize.EXTRA_SMALL,
                  color = TextPrimary.copy(alpha = Alpha.HALF),
                )
              }
            }
          }
          Text(
            text = "$${product.price}",
            fontSize = FontSize.EXTRA_REGULAR,
            color = TextBrand,
            fontWeight = FontWeight.Medium,
          )
        }
      }
    }
  }
}
