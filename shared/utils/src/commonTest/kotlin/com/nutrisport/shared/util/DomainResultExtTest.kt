package com.nutrisport.shared.util

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.startsWith
import kotlin.test.Test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class DomainResultExtTest {

    // --- AppError.fromException ---

    @Test
    fun `fromException should create Network error with message`() {
        val error = AppError.fromException(RuntimeException("timeout"))
        assertThat(error is AppError.Network).isEqualTo(true)
        assertThat(error.message).isEqualTo("timeout")
    }

    @Test
    fun `fromException should include context prefix`() {
        val error = AppError.fromException(RuntimeException("fail"), "updateCustomer")
        assertThat(error.message).startsWith("updateCustomer: ")
    }

    @Test
    fun `fromException should handle null message`() {
        val error = AppError.fromException(RuntimeException())
        assertThat(error.message).isEqualTo("Unknown error")
    }

    // --- domainResult ---

    @Test
    fun `domainResult should wrap success in Right`() {
        val result = domainResult { 42 }
        assertThat(result).isEqualTo(Either.Right(42))
    }

    @Test
    fun `domainResult should wrap exception in Left`() {
        val result = domainResult<Int> { throw RuntimeException("boom") }
        assertThat(result.isLeft).isEqualTo(true)
        val error = result.leftOrNull()
        assertThat(error).isNotNull()
        assertThat(error is AppError.Network).isEqualTo(true)
        assertThat(error!!.message).isEqualTo("boom")
    }

    // --- asDomainResult ---

    @Test
    fun `asDomainResult should emit Right for each value`() = runTest {
        flowOf(1, 2, 3).asDomainResult().test {
            assertThat(awaitItem()).isEqualTo(Either.Right(1))
            assertThat(awaitItem()).isEqualTo(Either.Right(2))
            assertThat(awaitItem()).isEqualTo(Either.Right(3))
            awaitComplete()
        }
    }

    @Test
    fun `asDomainResult should emit Left on exception`() = runTest {
        flow<Int> { throw RuntimeException("flow error") }
            .asDomainResult()
            .test {
                val item = awaitItem()
                assertThat(item.isLeft).isEqualTo(true)
                val error = item.leftOrNull()
                assertThat(error).isNotNull()
                assertThat(error is AppError.Network).isEqualTo(true)
                assertThat(error!!.message).isEqualTo("flow error")
                awaitComplete()
            }
    }

    @Test
    fun `asDomainResult should emit values then Left on mid-flow error`() = runTest {
        flow {
            emit(1)
            throw RuntimeException("mid error")
        }.asDomainResult().test {
            assertThat(awaitItem()).isEqualTo(Either.Right(1))
            val error = awaitItem()
            assertThat(error.isLeft).isEqualTo(true)
            awaitComplete()
        }
    }
}
