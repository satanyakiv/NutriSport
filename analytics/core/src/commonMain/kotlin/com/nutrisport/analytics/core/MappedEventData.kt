package com.nutrisport.analytics.core

data class MappedEventData(
  val eventName: String,
  val params: Map<String, Any>,
)
