package com.nutrisport.shared.domain

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class CartItem @OptIn(ExperimentalUuidApi::class) constructor(
  val id: String = Uuid.random().toHexString(),
  val productId: String,
  val flavor: String? = null,
  val quantity: Int,
)
