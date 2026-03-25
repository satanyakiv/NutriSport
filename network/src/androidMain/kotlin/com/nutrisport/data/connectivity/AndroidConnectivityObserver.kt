package com.nutrisport.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.nutrisport.shared.domain.ConnectivityObserver
import com.nutrisport.shared.domain.ConnectivityStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class AndroidConnectivityObserver(
  context: Context,
) : ConnectivityObserver {
  private val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  override val status: Flow<ConnectivityStatus> = callbackFlow {
    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        trySend(ConnectivityStatus.Available)
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
        trySend(ConnectivityStatus.Losing)
      }

      override fun onLost(network: Network) {
        trySend(ConnectivityStatus.Unavailable)
      }
    }

    val initialStatus = if (connectivityManager.activeNetwork != null) {
      ConnectivityStatus.Available
    } else {
      ConnectivityStatus.Unavailable
    }
    trySend(initialStatus)

    connectivityManager.registerDefaultNetworkCallback(callback)
    awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
  }.distinctUntilChanged()
}
