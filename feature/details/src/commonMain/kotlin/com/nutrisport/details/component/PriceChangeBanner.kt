package com.nutrisport.details.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisport.shared.SurfaceBrand
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextWhite

@Composable
fun PriceChangeBanner(
  previousPrice: String,
  currentPrice: String,
  isPriceIncrease: Boolean,
  modifier: Modifier = Modifier,
) {
  val backgroundColor = if (isPriceIncrease) SurfaceError else SurfaceBrand
  val textColor = if (isPriceIncrease) TextWhite else TextPrimary

  AnimatedVisibility(
    visible = true,
    enter = expandVertically(),
    exit = shrinkVertically(),
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .background(backgroundColor)
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .testTag("price_change_banner"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Text(
        text = "Price changed: ",
        color = textColor,
        fontSize = 13.sp,
      )
      Text(
        text = previousPrice,
        color = textColor.copy(alpha = 0.7f),
        fontSize = 13.sp,
        textDecoration = TextDecoration.LineThrough,
      )
      Text(
        text = " → ",
        color = textColor,
        fontSize = 13.sp,
      )
      Text(
        text = currentPrice,
        color = textColor,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
