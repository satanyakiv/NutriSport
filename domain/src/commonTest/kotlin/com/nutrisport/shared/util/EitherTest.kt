package com.nutrisport.shared.util

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

class EitherTest {

    @Test
    fun `Right should hold value`() {
        val result: Either<String, Int> = Either.Right(42)
        assertThat(result.isRight).isTrue()
        assertThat(result.getOrNull()).isEqualTo(42)
    }

    @Test
    fun `Left should hold error`() {
        val result: Either<String, Int> = Either.Left("error")
        assertThat(result.isLeft).isTrue()
        assertThat(result.leftOrNull()).isEqualTo("error")
        assertThat(result.getOrNull()).isNull()
    }

    @Test
    fun `fold should apply ifRight for Right`() {
        val result: Either<String, Int> = Either.Right(10)
        val folded = result.fold(ifLeft = { "fail" }, ifRight = { "ok: $it" })
        assertThat(folded).isEqualTo("ok: 10")
    }

    @Test
    fun `fold should apply ifLeft for Left`() {
        val result: Either<String, Int> = Either.Left("err")
        val folded = result.fold(ifLeft = { "fail: $it" }, ifRight = { "ok" })
        assertThat(folded).isEqualTo("fail: err")
    }

    @Test
    fun `map should transform Right value`() {
        val result: Either<String, Int> = Either.Right(5)
        val mapped = result.map { it * 2 }
        assertThat(mapped).isEqualTo(Either.Right(10))
    }

    @Test
    fun `map should not transform Left`() {
        val result: Either<String, Int> = Either.Left("err")
        val mapped = result.map { it * 2 }
        assertThat(mapped).isInstanceOf<Either.Left<String>>()
        assertThat(mapped.leftOrNull()).isEqualTo("err")
    }

    @Test
    fun `mapLeft should transform Left value`() {
        val result: Either<String, Int> = Either.Left("err")
        val mapped = result.mapLeft { it.uppercase() }
        assertThat(mapped).isEqualTo(Either.Left("ERR"))
    }

    @Test
    fun `flatMap should chain Right values`() {
        val result: Either<String, Int> = Either.Right(5)
        val chained = result.flatMap { Either.Right(it + 1) }
        assertThat(chained).isEqualTo(Either.Right(6))
    }

    @Test
    fun `flatMap should short-circuit on Left`() {
        val result: Either<String, Int> = Either.Left("err")
        val chained = result.flatMap { Either.Right(it + 1) }
        assertThat(chained.isLeft).isTrue()
    }

    // --- getOrElse ---

    @Test
    fun `getOrElse should return Right value`() {
        val result: Either<String, Int> = Either.Right(42)
        assertThat(result.getOrElse { -1 }).isEqualTo(42)
    }

    @Test
    fun `getOrElse should return default for Left`() {
        val result: Either<String, Int> = Either.Left("err")
        assertThat(result.getOrElse { it.length }).isEqualTo(3)
    }

    // --- onRight / onLeft ---

    @Test
    fun `onRight should execute action for Right`() {
        var captured = 0
        val result: Either<String, Int> = Either.Right(10)
        val returned = result.onRight { captured = it }
        assertThat(captured).isEqualTo(10)
        assertThat(returned).isEqualTo(result)
    }

    @Test
    fun `onRight should not execute action for Left`() {
        var called = false
        val result: Either<String, Int> = Either.Left("err")
        result.onRight { called = true }
        assertThat(called).isFalse()
    }

    @Test
    fun `onLeft should execute action for Left`() {
        var captured = ""
        val result: Either<String, Int> = Either.Left("err")
        val returned = result.onLeft { captured = it }
        assertThat(captured).isEqualTo("err")
        assertThat(returned).isEqualTo(result)
    }

    @Test
    fun `onLeft should not execute action for Right`() {
        var called = false
        val result: Either<String, Int> = Either.Right(1)
        result.onLeft { called = true }
        assertThat(called).isFalse()
    }

    // --- recover ---

    @Test
    fun `recover should transform Left to Right`() {
        val result: Either<String, Int> = Either.Left("err")
        val recovered = result.recover { it.length }
        assertThat(recovered).isEqualTo(Either.Right(3))
    }

    @Test
    fun `recover should keep Right unchanged`() {
        val result: Either<String, Int> = Either.Right(42)
        val recovered = result.recover { -1 }
        assertThat(recovered).isEqualTo(Either.Right(42))
    }

    // --- flatMapLeft ---

    @Test
    fun `flatMapLeft should transform Left`() {
        val result: Either<String, Int> = Either.Left("err")
        val mapped = result.flatMapLeft { Either.Left(it.length) }
        assertThat(mapped).isEqualTo(Either.Left(3))
    }

    @Test
    fun `flatMapLeft should recover Left to Right`() {
        val result: Either<String, Int> = Either.Left("err")
        val mapped = result.flatMapLeft { Either.Right(0) }
        assertThat(mapped).isEqualTo(Either.Right(0))
    }

    @Test
    fun `flatMapLeft should keep Right unchanged`() {
        val result: Either<String, Int> = Either.Right(42)
        val mapped = result.flatMapLeft { Either.Left("new") }
        assertThat(mapped).isEqualTo(Either.Right(42))
    }

    // --- combine ---

    @Test
    fun `combine should merge two Rights`() {
        val a: Either<String, Int> = Either.Right(2)
        val b: Either<String, Int> = Either.Right(3)
        val combined = a.combine(b) { x, y -> x + y }
        assertThat(combined).isEqualTo(Either.Right(5))
    }

    @Test
    fun `combine should return first Left when first is Left`() {
        val a: Either<String, Int> = Either.Left("first")
        val b: Either<String, Int> = Either.Right(3)
        val combined = a.combine(b) { x, y -> x + y }
        assertThat(combined).isEqualTo(Either.Left("first"))
    }

    @Test
    fun `combine should return second Left when second is Left`() {
        val a: Either<String, Int> = Either.Right(2)
        val b: Either<String, Int> = Either.Left("second")
        val combined = a.combine(b) { x, y -> x + y }
        assertThat(combined).isEqualTo(Either.Left("second"))
    }

    // --- toRight / toLeft ---

    @Test
    fun `toRight should wrap non-null in Right`() {
        val value: Int? = 42
        assertThat(value.toRight { "null" }).isEqualTo(Either.Right(42))
    }

    @Test
    fun `toRight should return Left for null`() {
        val value: Int? = null
        assertThat(value.toRight { "null" }).isEqualTo(Either.Left("null"))
    }

    @Test
    fun `toLeft should wrap non-null in Left`() {
        val value: String? = "err"
        assertThat(value.toLeft { 0 }).isEqualTo(Either.Left("err"))
    }

    @Test
    fun `toLeft should return Right for null`() {
        val value: String? = null
        assertThat(value.toLeft { 0 }).isEqualTo(Either.Right(0))
    }
}
