package com.nutrisport.manage_product.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutrisport.manage_product.ManageProductState
import com.nutrisport.shared.component.AlertTextField
import com.nutrisport.shared.component.CustomTextField
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.util.orZero

@Composable
internal fun ProductFormFields(
  screenState: ManageProductState,
  onTitleChange: (String) -> Unit,
  onDescriptionChange: (String) -> Unit,
  onCategoryClick: () -> Unit,
  onWeightChange: (Int?) -> Unit,
  onFlavorsChange: (String?) -> Unit,
  onPriceChange: (Double) -> Unit,
  onIsNewChange: (Boolean) -> Unit,
  onIsPopularChange: (Boolean) -> Unit,
  onIsDiscountedChange: (Boolean) -> Unit,
) {
  CustomTextField(
    value = screenState.title,
    onValueChange = onTitleChange,
    placeholder = "Title",
  )
  CustomTextField(
    modifier = Modifier.height(120.dp),
    value = screenState.description,
    onValueChange = onDescriptionChange,
    placeholder = "Description",
    expanded = true,
  )
  AlertTextField(
    modifier = Modifier.fillMaxWidth(),
    text = screenState.category.title,
    onClick = onCategoryClick,
  )
  AnimatedVisibility(
    visible = screenState.category != ProductCategory.Accessories,
  ) {
    Column {
      CustomTextField(
        value = screenState.weight?.toString().orEmpty(),
        onValueChange = { onWeightChange(it.toIntOrNull().orZero()) },
        placeholder = "Weight",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      )
      Spacer(Modifier.height(12.dp))
      CustomTextField(
        value = screenState.flavors.orEmpty(),
        onValueChange = { onFlavorsChange(it) },
        placeholder = "Flavors",
      )
    }
  }
  CustomTextField(
    value = screenState.price.toString(),
    onValueChange = {
      if (it.toDoubleOrNull() != null) {
        onPriceChange(it.toDoubleOrNull().orZero())
      }
    },
    placeholder = "Price",
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
  )
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    ProductSwitchRow(
      label = "New",
      checked = screenState.isNew,
      onCheckedChange = onIsNewChange,
    )
    ProductSwitchRow(
      label = "Popular",
      checked = screenState.isPopular,
      onCheckedChange = onIsPopularChange,
    )
    ProductSwitchRow(
      label = "Discounted",
      checked = screenState.isDiscounted,
      onCheckedChange = onIsDiscountedChange,
    )
  }
}
