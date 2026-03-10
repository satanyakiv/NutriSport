package com.nutrisport.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nutrisport.home.domain.DrawerItem
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.IconPrimary
import com.nutrisport.shared.TextPrimary
import org.jetbrains.compose.resources.painterResource

@Composable
fun DrawerItemCard(
  drawerItem: DrawerItem,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(99.dp))
      .clickable(onClick = onClick)
      .padding(12.dp),
  ) {
    Icon(
      painter = painterResource(drawerItem.icon),
      contentDescription = drawerItem.title,
      tint = IconPrimary,
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = drawerItem.title,
      color = TextPrimary,
      fontSize = FontSize.EXTRA_REGULAR,
    )
  }
}
