package com.nutrisport.shared.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Canonical wrapper for `@Preview` functions. Reuse this instead of inlining
 * `MaterialTheme { Surface { ... } }` in every preview. See `.claude/rules/preview.md`.
 */
@Composable
fun NutriSportPreview(
  padding: Int = 16,
  content: @Composable () -> Unit,
) {
  MaterialTheme {
    Surface {
      Box(modifier = Modifier.padding(padding.dp)) {
        content()
      }
    }
  }
}
