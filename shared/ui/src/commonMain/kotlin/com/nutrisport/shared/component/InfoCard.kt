package com.nutrisport.shared.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Resources
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun InfoCard(
  modifier: Modifier = Modifier,
  image: DrawableResource,
  title: String,
  subtitle: String,
) {
  Column(
    modifier = modifier
      .fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Image(
      modifier = Modifier.size(60.dp),
      painter = painterResource(image),
      contentDescription = "info card image"
    )
    Spacer(modifier = Modifier.height(24.dp))
    Text(
      text = title,
      fontSize = FontSize.MEDIUM,
      fontWeight = FontWeight.Bold,
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      modifier = Modifier.fillMaxWidth(),
      text = subtitle,
      fontSize = FontSize.REGULAR,
      textAlign = TextAlign.Center,
    )
  }
}

@Suppress("UnusedPrivateMember")
@Preview
@Composable
private fun InfoCardPreview() {
  InfoCard(
    image = Resources.Image.ShoppingCart,
    title = "Title",
    subtitle = "unum",
  )
}
