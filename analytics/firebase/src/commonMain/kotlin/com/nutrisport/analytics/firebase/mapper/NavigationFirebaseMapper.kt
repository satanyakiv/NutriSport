package com.nutrisport.analytics.firebase.mapper

import com.nutrisport.analytics.core.AnalyticsEvent
import com.nutrisport.analytics.core.AnalyticsEventMapper
import com.nutrisport.analytics.core.MappedEventData
import com.nutrisport.analytics.core.NavigationEvent

class NavigationFirebaseMapper : AnalyticsEventMapper {

  override fun map(event: AnalyticsEvent): MappedEventData? {
    if (event !is NavigationEvent) return null
    return when (event) {
      is NavigationEvent.ScreenView -> MappedEventData(
        eventName = event.name,
        params = mapOf("screen_name" to event.screenName),
      )
    }
  }
}
