package com.nutrisport.data.connectivity

import com.nutrisport.shared.domain.ConnectivityObserver
import com.nutrisport.shared.domain.ConnectivityStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

class IosConnectivityObserver : ConnectivityObserver {
  override val status: Flow<ConnectivityStatus> = callbackFlow {
    val monitor = nw_path_monitor_create()
    nw_path_monitor_set_update_handler(monitor) { path ->
      val connectivityStatus = if (nw_path_get_status(path) == nw_path_status_satisfied) {
        ConnectivityStatus.Available
      } else {
        ConnectivityStatus.Unavailable
      }
      trySend(connectivityStatus)
    }
    nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
    nw_path_monitor_start(monitor)
    awaitClose { nw_path_monitor_cancel(monitor) }
  }.distinctUntilChanged()
}
