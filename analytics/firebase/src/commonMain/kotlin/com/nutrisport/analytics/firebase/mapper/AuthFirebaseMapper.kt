package com.nutrisport.analytics.firebase.mapper

import com.nutrisport.analytics.core.AnalyticsEvent
import com.nutrisport.analytics.core.AnalyticsEventMapper
import com.nutrisport.analytics.core.AuthEvent
import com.nutrisport.analytics.core.MappedEventData

class AuthFirebaseMapper : AnalyticsEventMapper {

  override fun map(event: AnalyticsEvent): MappedEventData? {
    if (event !is AuthEvent) return null
    return when (event) {
      is AuthEvent.SignIn -> MappedEventData(
        eventName = event.name,
        params = mapOf("method" to event.method),
      )
      is AuthEvent.SignOut -> MappedEventData(
        eventName = event.name,
        params = emptyMap(),
      )
    }
  }
}
