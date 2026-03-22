package com.nutrisport.analytics.firebase.mapper

import com.nutrisport.analytics.core.AnalyticsEvent
import com.nutrisport.analytics.core.AnalyticsEventMapper
import com.nutrisport.analytics.core.EcommerceEvent
import com.nutrisport.analytics.core.MappedEventData

class EcommerceFirebaseMapper : AnalyticsEventMapper {

  override fun map(event: AnalyticsEvent): MappedEventData? {
    if (event !is EcommerceEvent) return null
    return when (event) {
      is EcommerceEvent.AddToCart -> MappedEventData(
        eventName = event.name,
        params = mapOf(
          "product_id" to event.productId,
          "product_name" to event.productName,
          "price" to event.price,
          "quantity" to event.quantity,
          "category" to event.category,
        ),
      )
      is EcommerceEvent.RemoveFromCart -> MappedEventData(
        eventName = event.name,
        params = mapOf(
          "product_id" to event.productId,
          "product_name" to event.productName,
        ),
      )
      is EcommerceEvent.ViewCart -> MappedEventData(
        eventName = event.name,
        params = mapOf("item_count" to event.itemCount),
      )
      is EcommerceEvent.Purchase -> MappedEventData(
        eventName = event.name,
        params = mapOf(
          "order_id" to event.orderId,
          "total_amount" to event.totalAmount,
          "item_count" to event.itemCount,
        ),
      )
      is EcommerceEvent.ViewItem -> MappedEventData(
        eventName = event.name,
        params = mapOf(
          "product_id" to event.productId,
          "product_name" to event.productName,
          "price" to event.price,
          "category" to event.category,
        ),
      )
      is EcommerceEvent.Search -> MappedEventData(
        eventName = event.name,
        params = mapOf("query" to event.query),
      )
    }
  }
}
