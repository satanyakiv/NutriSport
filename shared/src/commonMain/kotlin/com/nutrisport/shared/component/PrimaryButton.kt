package com.nutrisport.shared.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Alpha.DISABLED
import com.nutrisport.shared.ButtonDisabled
import com.nutrisport.shared.ButtonPrimary
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PrimaryButton(
  modifier: Modifier = Modifier,
  text: String,
  icon: DrawableResource? = null,
  enabled: Boolean = true,
  onClick: () -> Unit,
) {
  Button(
    modifier = modifier.fillMaxWidth(),
    onClick = onClick,
    enabled = enabled,
    shape = RoundedCornerShape(6.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = ButtonPrimary,
      contentColor = TextPrimary,
      disabledContainerColor = ButtonDisabled,
      disabledContentColor = TextPrimary.copy(alpha = DISABLED),
    ),
    contentPadding = PaddingValues(all = 20.dp),
  ) {
    if (icon != null) {
      Icon(
        painter = painterResource(icon),
        contentDescription = "Button icon",
        modifier = Modifier.size(14.dp),
      )
      Spacer(modifier = Modifier.size(12.dp))
    }
    Text(
      text = text,
      fontSize = FontSize.REGULAR,
      fontWeight = FontWeight.Medium,
    )
  }
}