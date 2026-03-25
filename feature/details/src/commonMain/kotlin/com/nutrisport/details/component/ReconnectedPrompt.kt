package com.nutrisport.details.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.nutrisport.shared.TextPrimary

@Composable
fun ReconnectedPrompt(
  visible: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AnimatedVisibility(
    visible = visible,
    enter = expandVertically(),
    exit = shrinkVertically(),
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .background(SurfaceBrand)
        .padding(horizontal = 16.dp, vertical = 10.dp)
        .testTag("reconnected_prompt"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Text(
        text = "Back online!",
        color = TextPrimary,
        fontSize = 13.sp,
      )
      Text(
        text = "Refresh prices",
        color = TextPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
          .clickable { onRefresh() }
          .testTag("refresh_button"),
      )
    }
  }
}
