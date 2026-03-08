package com.nutrisport.shared.domain.usecase

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.nutrisport.shared.domain.PhoneNumber
import kotlin.test.Test

class ValidateProfileFormUseCaseTest {

    private val useCase = ValidateProfileFormUseCase()

    private fun validForm() = mapOf(
        "firstName" to "John",
        "lastName" to "Doe",
        "city" to "Belgrade",
        "postalCode" to 11000,
        "address" to "Main Street 1",
        "phoneNumber" to PhoneNumber(dialCode = 381, number = "612345678"),
    )

    private fun invokeWith(
        firstName: String = validForm()["firstName"] as String,
        lastName: String = validForm()["lastName"] as String,
        city: String? = validForm()["city"] as String,
        postalCode: Int? = validForm()["postalCode"] as Int,
        address: String? = validForm()["address"] as String,
        phoneNumber: PhoneNumber? = validForm()["phoneNumber"] as PhoneNumber,
    ) = useCase(firstName, lastName, city, postalCode, address, phoneNumber)

    @Test
    fun `should return true for valid form`() {
        assertThat(invokeWith()).isTrue()
    }

    @Test
    fun `should return false when firstName too short`() {
        assertThat(invokeWith(firstName = "Jo")).isFalse()
    }

    @Test
    fun `should return false when lastName too short`() {
        assertThat(invokeWith(lastName = "D")).isFalse()
    }

    @Test
    fun `should return false when city is null`() {
        assertThat(invokeWith(city = null)).isFalse()
    }

    @Test
    fun `should return false when city too short`() {
        assertThat(invokeWith(city = "NY")).isFalse()
    }

    @Test
    fun `should return false when postalCode is null`() {
        assertThat(invokeWith(postalCode = null)).isFalse()
    }

    @Test
    fun `should return false when postalCode too short`() {
        assertThat(invokeWith(postalCode = 12)).isFalse()
    }

    @Test
    fun `should return false when address is null`() {
        assertThat(invokeWith(address = null)).isFalse()
    }

    @Test
    fun `should return false when address too short`() {
        assertThat(invokeWith(address = "AB")).isFalse()
    }

    @Test
    fun `should return false when phoneNumber is null`() {
        assertThat(invokeWith(phoneNumber = null)).isFalse()
    }

    @Test
    fun `should return false when phone number too short`() {
        assertThat(invokeWith(phoneNumber = PhoneNumber(381, "1234"))).isFalse()
    }
}
