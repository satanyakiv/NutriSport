package com.nutrisport.shared.component.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.Alpha.TWENTY_PERCENT
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.color
import org.jetbrains.compose.resources.painterResource

@Composable
fun CategoriesDialog(
  category: ProductCategory,
  onDismiss: () -> Unit,
  onConfirmClick: (ProductCategory) -> Unit,
) {
  var selectedCategory by remember { mutableStateOf(category) }

  AlertDialog(
    containerColor = Surface,
    title = {
      Text(
        text = "Select the category",
        fontSize = FontSize.EXTRA_MEDIUM,
        color = TextPrimary
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(300.dp)
      ) {
        ProductCategory.entries.forEach { currentCategory ->
          val animatedBackground by animateColorAsState(
            targetValue = if (currentCategory == selectedCategory) selectedCategory.color.copy(alpha = TWENTY_PERCENT) else Transparent
          )
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(animatedBackground)
              .clickable { selectedCategory = currentCategory }
              .padding(
                horizontal = 12.dp,
                vertical = 16.dp,
              )
          ) {
            Text(
              text = currentCategory.title,
              fontSize = FontSize.REGULAR,
              color = TextPrimary,
              modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(12.dp))
            AnimatedVisibility(
              visible = currentCategory == selectedCategory
            ) {
              Icon(
                painter = painterResource(Resources.Icon.Checkmark),
                contentDescription = "Checkmark icon",
                modifier = Modifier.size(14.dp),
                tint = IconPrimary,
              )
            }
          }
        }
      }
    },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = { onConfirmClick(selectedCategory) },
        colors = ButtonDefaults.textButtonColors(
          containerColor = Transparent,
          contentColor = TextSecondary
        )
      ) {
        Text(
          text = "Confirm",
          fontSize = FontSize.REGULAR,
          fontWeight = FontWeight.Medium
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        colors = ButtonDefaults.textButtonColors(
          containerColor = Transparent,
          contentColor = TextPrimary.copy(alpha = Alpha.HALF)
        )
      ) {
        Text(
          text = "Cancel",
          fontSize = FontSize.REGULAR,
          fontWeight = FontWeight.Medium
        )
      }
    }
  )
}
