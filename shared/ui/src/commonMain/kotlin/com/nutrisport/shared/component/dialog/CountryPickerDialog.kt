package com.nutrisport.shared.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.Surface
import com.nutrisport.shared.TextPrimary
import com.nutrisport.shared.TextSecondary
import com.nutrisport.shared.component.CustomTextField
import com.nutrisport.shared.component.ErrorCard
import com.nutrisport.shared.domain.Country

@Composable
fun CountryPickerDialog(
  country: Country,
  onDismiss: () -> Unit,
  onConfirmClick: (Country) -> Unit,
) {
  var selectedCountry by remember(country) { mutableStateOf(country) }
  val allCountries = remember { Country.entries.toList() }
  val filteredCountries = remember {
    mutableStateListOf<Country>().apply { addAll(allCountries) }
  }
  var searchQuery by remember { mutableStateOf("") }

  AlertDialog(
    containerColor = Surface,
    title = {
      Text(
        text = "Select a Country",
        fontSize = FontSize.EXTRA_MEDIUM,
        color = TextPrimary,
      )
    },
    text = {
      Column(
        modifier = Modifier.height(300.dp).fillMaxWidth(),
      ) {
        CustomTextField(
          value = searchQuery,
          onValueChange = { query ->
            searchQuery = query
            filteredCountries.clear()
            if (searchQuery.isNotBlank()) {
              filteredCountries.addAll(allCountries.filterByCountry(query))
            } else {
              filteredCountries.addAll(allCountries)
            }
          },
          placeholder = "Dial Code",
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (filteredCountries.isNotEmpty()) {
          LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            items(
              items = filteredCountries,
              key = { it.ordinal },
            ) { country ->
              CountryPickerItem(
                country = country,
                isSelected = selectedCountry == country,
                onSelect = { selectedCountry = country },
              )
            }
          }
        } else {
          ErrorCard(
            modifier = Modifier.weight(1f),
            message = "Dial code not found.",
          )
        }
      }
    },
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = { onConfirmClick(selectedCountry) },
        colors = ButtonDefaults.textButtonColors(
          containerColor = Color.Transparent,
          contentColor = TextSecondary,
        ),
      ) {
        Text(
          text = "Confirm",
          fontSize = FontSize.REGULAR,
          fontWeight = FontWeight.Medium,
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        colors = ButtonDefaults.textButtonColors(
          containerColor = Color.Transparent,
          contentColor = TextPrimary.copy(alpha = Alpha.HALF),
        ),
      ) {
        Text(
          text = "Cancel",
          fontSize = FontSize.REGULAR,
          fontWeight = FontWeight.Medium,
        )
      }
    },
  )
}
