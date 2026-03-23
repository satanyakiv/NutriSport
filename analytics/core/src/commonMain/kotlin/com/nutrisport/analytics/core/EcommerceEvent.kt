package com.nutrisport.analytics.core

sealed interface EcommerceEvent : AnalyticsEvent {

  data class AddToCart(
    val productId: String,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val category: String,
  ) : EcommerceEvent {
    override val name: String = "add_to_cart"
  }

  data class RemoveFromCart(
    val productId: String,
    val productName: String,
  ) : EcommerceEvent {
    override val name: String = "remove_from_cart"
  }

  data class ViewCart(val itemCount: Int) : EcommerceEvent {
    override val name: String = "view_cart"
  }

  data class Purchase(
    val orderId: String,
    val totalAmount: Double,
    val itemCount: Int,
  ) : EcommerceEvent {
    override val name: String = "purchase"
  }

  data class ViewItem(
    val productId: String,
    val productName: String,
    val price: Double,
    val category: String,
  ) : EcommerceEvent {
    override val name: String = "view_item"
  }

  data class Search(val query: String) : EcommerceEvent {
    override val name: String = "search"
  }
}
