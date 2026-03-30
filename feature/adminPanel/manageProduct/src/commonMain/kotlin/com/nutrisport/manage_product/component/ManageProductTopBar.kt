package com.nutrisport.manage_product.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.BebasNeueFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.Resources
import com.nutrisport.shared.Surface
import com.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManageProductTopBar(
  isEditMode: Boolean,
  onBack: () -> Unit,
  onDelete: () -> Unit,
) {
  var dropDownMenuOpen by remember { mutableStateOf(false) }

  TopAppBar(
    title = {
      Text(
        text = if (!isEditMode) "Add Product" else "Edit Product",
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
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = Surface,
      scrolledContainerColor = Surface,
      navigationIconContentColor = IconPrimary,
      titleContentColor = TextPrimary,
      actionIconContentColor = IconPrimary,
    ),
    actions = {
      if (isEditMode) {
        Box {
          IconButton(onClick = { dropDownMenuOpen = true }) {
            Icon(
              painter = painterResource(Resources.Icon.VerticalMenu),
              contentDescription = "Vertical menu icon",
              tint = IconPrimary,
            )
          }
          DropdownMenu(
            expanded = dropDownMenuOpen,
            onDismissRequest = { dropDownMenuOpen = false },
          ) {
            DropdownMenuItem(
              text = { Text("Delete", color = TextPrimary) },
              leadingIcon = {
                Icon(
                  modifier = Modifier.size(14.dp),
                  painter = painterResource(Resources.Icon.Delete),
                  contentDescription = "Delete icon",
                  tint = IconPrimary,
                )
              },
              onClick = {
                dropDownMenuOpen = false
                onDelete()
              },
            )
          }
        }
      }
    },
  )
}
