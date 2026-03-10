package com.nutrisport.details.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.component.QuantityCounter
import com.nutrisport.shared.domain.QuantityCounterSize
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailsTopBar(
  quantity: Int,
  onBack: () -> Unit,
  onUpdateQuantity: (Int) -> Unit,
) {
  TopAppBar(
    title = {
      Text(
        text = "Details",
        fontFamily = BebasNeueFont(),
        fontSize = FontSize.LARGE,
        color = TextPrimary,
      )
    },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(
          painter = painterResource(Resources.Icon.BackArrow),
          contentDescription = "Back Arrow icon",
          tint = IconPrimary,
        )
      }
    },
    actions = {
      QuantityCounter(
        size = QuantityCounterSize.Large,
        value = quantity,
        onMinusClick = onUpdateQuantity,
        onPlusClick = onUpdateQuantity,
      )
      Spacer(modifier = Modifier.width(16.dp))
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Surface,
      scrolledContainerColor = Surface,
      navigationIconContentColor = IconPrimary,
      titleContentColor = TextPrimary,
      actionIconContentColor = IconPrimary,
    ),
  )
}
