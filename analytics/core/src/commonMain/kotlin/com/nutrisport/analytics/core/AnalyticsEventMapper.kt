package com.nutrisport.analytics.core

interface AnalyticsEventMapper {
  fun map(event: AnalyticsEvent): MappedEventData?
}
