package com.nutrisport.shared.util

sealed class AppError(open val message: String) {
    data class Network(override val message: String) : AppError(message)
    data class NotFound(override val message: String) : AppError(message)
    data class Unauthorized(
        override val message: String = "User is not available",
    ) : AppError(message)
    data class Unknown(override val message: String) : AppError(message)
}

typealias DomainResult<T> = Either<AppError, T>
