package com.nutrisport.shared.util

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    val isLeft: Boolean get() = this is Left
    val isRight: Boolean get() = this is Right

    fun getOrNull(): R? = when (this) {
        is Left -> null
        is Right -> value
    }

    fun leftOrNull(): L? = when (this) {
        is Left -> value
        is Right -> null
    }

    inline fun <C> fold(ifLeft: (L) -> C, ifRight: (R) -> C): C = when (this) {
        is Left -> ifLeft(value)
        is Right -> ifRight(value)
    }

    inline fun <C> map(transform: (R) -> C): Either<L, C> = when (this) {
        is Left -> this
        is Right -> Right(transform(value))
    }

    inline fun <C> mapLeft(transform: (L) -> C): Either<C, R> = when (this) {
        is Left -> Left(transform(value))
        is Right -> this
    }

    inline fun <C> flatMap(transform: (R) -> Either<@UnsafeVariance L, C>): Either<L, C> =
        when (this) {
            is Left -> this
            is Right -> transform(value)
        }
}
