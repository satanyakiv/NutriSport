package com.nutrisport.shared.component.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconWhite
import com.nutrisport.shared.Resources
import com.nutrisport.shared.SurfaceLighter
import com.nutrisport.shared.SurfaceSecondary
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.domain.Country
import com.nutrisport.shared.domain.flag
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun CountryPickerItem(
  country: Country,
  isSelected: Boolean,
  onSelect: () -> Unit,
) {
  val saturation = remember { Animatable(if (isSelected) 1f else 0f) }

  LaunchedEffect(isSelected) {
    saturation.animateTo(if (isSelected) 1f else 0f)
  }

  val colorMatrix = remember(saturation.value) {
    ColorMatrix().apply { setToSaturation(saturation.value) }
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onSelect() }
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      modifier = Modifier.size(14.dp),
      painter = painterResource(country.flag),
      contentDescription = "Country flag image",
      colorFilter = ColorFilter.colorMatrix(colorMatrix),
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      modifier = Modifier.weight(1f),
      text = "+${country.dialCode} (${country.name})",
      fontSize = FontSize.REGULAR,
      color = TextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
    Selector(isSelected = isSelected)
  }
}

@Composable
private fun Selector(isSelected: Boolean) {
  val animatedBackground by animateColorAsState(
    targetValue = if (isSelected) SurfaceSecondary else SurfaceLighter,
  )
  Box(
    modifier = Modifier
      .size(20.dp)
      .clip(CircleShape)
      .background(animatedBackground),
    contentAlignment = Alignment.Center,
  ) {
    AnimatedVisibility(visible = isSelected) {
      Icon(
        modifier = Modifier.size(14.dp),
        painter = painterResource(Resources.Icon.Checkmark),
        contentDescription = "Checkmark icon",
        tint = IconWhite,
      )
    }
  }
}

internal fun List<Country>.filterByCountry(query: String): List<Country> {
  val queryLower = query.lowercase()
  val queryInt = query.toIntOrNull()
  return filter {
    it.name.lowercase().contains(queryLower) ||
        (queryInt != null && it.dialCode == queryInt)
  }
}
