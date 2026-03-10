package com.nutrisport.analytics

sealed class AnalyticsEvent(
  val name: String,
  val params: Map<String, Any> = emptyMap(),
) {
  data class ScreenView(val screenName: String) : AnalyticsEvent(
    name = "screen_view",
    params = mapOf("screen_name" to screenName),
  )

  data class AddToCart(
    val productId: String,
    val productName: String,
    val price: Double,
  ) : AnalyticsEvent(
    name = "add_to_cart",
    params = mapOf(
      "product_id" to productId,
      "product_name" to productName,
      "price" to price,
    ),
  )

  data class RemoveFromCart(val productId: String) : AnalyticsEvent(
    name = "remove_from_cart",
    params = mapOf("product_id" to productId),
  )

  data class Purchase(
    val orderId: String,
    val totalAmount: Double,
    val itemCount: Int,
  ) : AnalyticsEvent(
    name = "purchase",
    params = mapOf(
      "order_id" to orderId,
      "total_amount" to totalAmount,
      "item_count" to itemCount,
    ),
  )

  data object SignIn : AnalyticsEvent(name = "sign_in")

  data object SignOut : AnalyticsEvent(name = "sign_out")

  data class Search(val query: String) : AnalyticsEvent(
    name = "search",
    params = mapOf("query" to query),
  )

  data class ProductView(val productId: String) : AnalyticsEvent(
    name = "product_view",
    params = mapOf("product_id" to productId),
  )
}
