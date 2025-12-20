package com.nutrisport.home.domain

import com.nutrisport.shared.Resources
import org.jetbrains.compose.resources.DrawableResource

enum class DrawerItem(
  val title: String,
  val icon: DrawableResource,
) {
  Profile("Profile", Resources.Icon.Person),
  Blog("Blog", Resources.Icon.Book),
  Locations("Locations", Resources.Icon.MapPin),
  Contact("Contact us", Resources.Icon.Edit),
  SignOut("Sign out", Resources.Icon.SignOut),
  Admin("Admin panel", Resources.Icon.Unlock)
}
