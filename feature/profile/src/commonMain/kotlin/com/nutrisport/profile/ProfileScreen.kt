package com.nutrisport.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Resources
import com.nutrisport.shared.composent.PrimaryButton

@Composable
fun ProfileScreen(
  modifier: Modifier = Modifier,

) {
  Column(
    modifier = Modifier.fillMaxSize().padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,


  ) {
    PrimaryButton(
      modifier = Modifier,
      "Continue",
      onClick = {},
      icon = Resources.Icon.Checkmark,
    )
  }
}