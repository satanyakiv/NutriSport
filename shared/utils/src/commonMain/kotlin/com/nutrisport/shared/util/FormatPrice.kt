package com.nutrisport.shared.util

import kotlin.math.roundToInt

fun Double.formatPrice(): String {
    val cents = (this * 100).roundToInt()
    val wholePart = cents / 100
    val fracPart = (cents % 100).toString().padStart(2, '0')
    return "$${wholePart}.${fracPart}"
}
