package com.nutrisport.details.model

import com.nutrisport.shared.domain.ConnectivityStatus
import com.nutrisport.shared.util.UiState

data class DetailsScreenState(
  val product: UiState<ProductUi> = UiState.Loading,
  val connectivity: ConnectivityStatus = ConnectivityStatus.Available,
  val showReconnectedPrompt: Boolean = false,
)
