package com.nutrisport.shared.test

import com.nutrisport.shared.domain.ConnectivityObserver
import com.nutrisport.shared.domain.ConnectivityStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityObserver(
  initialStatus: ConnectivityStatus = ConnectivityStatus.Available,
) : ConnectivityObserver {
  private val _status = MutableStateFlow(initialStatus)
  override val status: Flow<ConnectivityStatus> = _status

  fun emit(newStatus: ConnectivityStatus) {
    _status.value = newStatus
  }
}
