package com.nutrisport.shared.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed class AppError(open val message: String) {
  data class Network(override val message: String) : AppError(message)
  data class NotFound(override val message: String) : AppError(message)
  data class Unauthorized(
    override val message: String = "User is not available",
  ) : AppError(message)
  data class Unknown(override val message: String) : AppError(message)

  companion object {
    /** Створює AppError з Exception. */
    fun fromException(e: Exception, context: String = ""): AppError {
      val prefix = if (context.isNotEmpty()) "$context: " else ""
      return Network("$prefix${e.message ?: "Unknown error"}")
    }
  }
}

typealias DomainResult<T> = Either<AppError, T>

/** Виконує [block] і обгортає результат у Right, або ловить Exception і повертає Left(AppError). */
inline fun <T> domainResult(block: () -> T): DomainResult<T> = try {
  Either.Right(block())
} catch (e: Exception) {
  Either.Left(AppError.fromException(e))
}

/** Обгортає Flow у DomainResult: кожен елемент → Right, exceptions → Left(AppError). */
fun <T> Flow<T>.asDomainResult(): Flow<DomainResult<T>> =
  map<T, DomainResult<T>> { Either.Right(it) }
    .catch { e ->
      val error = if (e is Exception) {
        AppError.fromException(e)
      } else {
        AppError.Unknown(e.message ?: "Unknown error")
      }
      emit(Either.Left(error))
    }
