package com.nutrisport.shared.util

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Content<out T>(val result: DomainResult<T>) : UiState<T>()

    fun isLoading(): Boolean = this is Loading
    fun isIdle(): Boolean = this is Idle

    fun getSuccessDataOrNull(): T? = when (this) {
        is Content -> result.getOrNull()
        else -> null
    }

    fun getErrorMessageOrNull(): String? = when (this) {
        is Content -> result.leftOrNull()?.message
        else -> null
    }

    fun <R> map(transform: (T) -> R): UiState<R> = when (this) {
        is Idle -> Idle
        is Loading -> Loading
        is Content -> Content(result.map(transform))
    }
}
