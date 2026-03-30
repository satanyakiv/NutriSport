package com.nutrisport.shared.domain

enum class Country(
  val dialCode: Int,
  val code: String,
) {
  Ukraine(dialCode = 380, code = "UA"),
  India(dialCode = 91, code = "IN"),
  Usa(dialCode = 1, code = "US"),
}
