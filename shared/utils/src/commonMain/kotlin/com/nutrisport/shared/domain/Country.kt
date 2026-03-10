package com.nutrisport.shared.domain

enum class Country(
  val dialCode: Int,
  val code: String,
) {
  Serbia(dialCode = 381, code = "RS"),
  India(dialCode = 91, code = "IN"),
  Usa(dialCode = 1, code = "US"),
}
