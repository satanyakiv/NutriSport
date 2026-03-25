package com.nutrisport.shared.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutrisport.shared.Resources
import com.nutrisport.shared.SurfaceError
import com.nutrisport.shared.TextWhite
import org.jetbrains.compose.resources.painterResource

@Composable
fun OfflineBanner(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(SurfaceError)
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("offline_banner"),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(Resources.Icon.AlertTriangle),
      contentDescription = null,
      tint = TextWhite,
      modifier = Modifier.size(16.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = "No internet connection",
      color = TextWhite,
      fontSize = 13.sp,
    )
  }
}
