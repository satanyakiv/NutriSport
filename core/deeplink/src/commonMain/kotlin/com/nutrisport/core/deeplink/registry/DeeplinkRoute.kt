package com.nutrisport.core.deeplink.registry

import com.nutrisport.core.deeplink.DeeplinkGate
import com.nutrisport.shared.navigation.Screen

typealias PathParams = Map<String, String>
typealias QueryParams = Map<String, String>

/**
 * One row in the deeplink registry. [pathTemplate] is matched literally segment
 * by segment; segments wrapped in braces (`{id}`) capture path parameters into
 * [PathParams]. [resolve] receives both captures and query params and returns a
 * concrete [Screen] (or null if the captured values are invalid). [gate] is the
 * access tier the target requires.
 */
data class DeeplinkRoute(
  val pathTemplate: String,
  val gate: DeeplinkGate,
  val resolve: (PathParams, QueryParams) -> Screen?,
)
