package com.nutrisport.shared.util

sealed class RequestState<out T> {
  data object Idle : RequestState<Nothing>()
  data object Loading : RequestState<Nothing>()
  data class Success<out T>(val data: T) : RequestState<T>()
  data class Error(val message: String) : RequestState<Nothing>()

  fun isIdle(): Boolean = this is Idle
  fun isLoading(): Boolean = this is Loading
  fun isError(): Boolean = this is Error
  fun isSuccess(): Boolean = this is Success

  fun getSuccessData() = (this as Success).data
  fun getSuccessDataOrNull() = if (this.isSuccess()) this.getSuccessData() else null
  fun getErrorMessage(): String = (this as Error).message
}
