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

/** Повертає Right value або обчислює default з Left value. */
inline fun <L, R> Either<L, R>.getOrElse(default: (L) -> R): R = when (this) {
  is Either.Left -> default(value)
  is Either.Right -> value
}

/** Виконує [action] якщо це Right, повертає оригінальний Either. */
inline fun <L, R> Either<L, R>.onRight(action: (R) -> Unit): Either<L, R> = also {
  if (it is Either.Right) action(it.value)
}

/** Виконує [action] якщо це Left, повертає оригінальний Either. */
inline fun <L, R> Either<L, R>.onLeft(action: (L) -> Unit): Either<L, R> = also {
  if (it is Either.Left) action(it.value)
}

/** Перетворює Left на Right, дозволяючи "відновити" помилку значенням за замовчуванням. */
inline fun <L, R> Either<L, R>.recover(transform: (L) -> R): Either<Nothing, R> = when (this) {
  is Either.Left -> Either.Right(transform(value))
  is Either.Right -> @Suppress("UNCHECKED_CAST") (this as Either<Nothing, R>)
}

/** Монадичне chaining по Left гілці. */
inline fun <L, R, C> Either<L, R>.flatMapLeft(
  transform: (L) -> Either<C, R>,
): Either<C, R> = when (this) {
  is Either.Left -> transform(value)
  is Either.Right -> @Suppress("UNCHECKED_CAST") (this as Either<C, R>)
}

/** Комбінує два Either: якщо обидва Right — застосовує [transform], інакше повертає перший Left. */
inline fun <L, R1, R2, C> Either<L, R1>.combine(
  other: Either<L, R2>,
  transform: (R1, R2) -> C,
): Either<L, C> = when (this) {
  is Either.Left -> this
  is Either.Right -> when (other) {
    is Either.Left -> other
    is Either.Right -> Either.Right(transform(this.value, other.value))
  }
}

/** Обгортає non-null значення в Right, або повертає Left з [ifNull]. */
inline fun <L, R> R?.toRight(ifNull: () -> L): Either<L, R> =
  if (this != null) Either.Right(this) else Either.Left(ifNull())

/** Обгортає non-null значення в Left, або повертає Right з [ifNull]. */
inline fun <L, R> L?.toLeft(ifNull: () -> R): Either<L, R> =
  if (this != null) Either.Left(this) else Either.Right(ifNull())
