package com.nutrisport.manage_product.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Surface
import com.nutrisport.shared.SurfaceDarker
import com.nutrisport.shared.SurfaceSecondary
import com.nutrisport.shared.TextPrimary

@Composable
internal fun ProductSwitchRow(
  label: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = label,
      fontSize = FontSize.REGULAR,
      color = TextPrimary,
      modifier = Modifier.padding(start = 12.dp),
    )
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedTrackColor = SurfaceSecondary,
        uncheckedTrackColor = SurfaceDarker,
        checkedThumbColor = Surface,
        uncheckedThumbColor = Surface,
        checkedBorderColor = SurfaceSecondary,
        uncheckedBorderColor = SurfaceDarker,
      ),
    )
  }
}
