package com.nutrisport.shared.domain

import kotlinx.coroutines.flow.Flow

enum class ConnectivityStatus {
  Available,
  Unavailable,
  Losing,
}

interface ConnectivityObserver {
  val status: Flow<ConnectivityStatus>
}
