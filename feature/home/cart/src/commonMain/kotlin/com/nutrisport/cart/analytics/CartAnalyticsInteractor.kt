package com.nutrisport.cart.analytics

import com.nutrisport.analytics.core.EcommerceEvent
import com.nutrisport.analytics.core.NutriSportAnalytics

class CartAnalyticsInteractor(
  private val analytics: NutriSportAnalytics,
) {

  suspend fun logRemoveFromCart(
    productId: String,
    productName: String,
  ) {
    analytics.logEvent(
      EcommerceEvent.RemoveFromCart(
        productId = productId,
        productName = productName,
      ),
    )
  }

  suspend fun logViewCart(itemCount: Int) {
    analytics.logEvent(EcommerceEvent.ViewCart(itemCount = itemCount))
  }
}
