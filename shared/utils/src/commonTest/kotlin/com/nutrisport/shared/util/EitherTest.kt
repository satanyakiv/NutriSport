package com.nutrisport.shared.util

import assertk.assertThat
import assertk.assertions.isEqualTo
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
}
