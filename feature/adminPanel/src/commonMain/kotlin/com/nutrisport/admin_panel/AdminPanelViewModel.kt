package com.nutrisport.admin_panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.AdminRepository
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AdminPanelViewModel(
  private val adminRepository: AdminRepository,
) : ViewModel() {
  private val allProducts = adminRepository.readLastTenProducts()
    .map { UiState.Content(it) }
    .onStart<UiState<List<com.nutrisport.shared.domain.Product>>> { emit(UiState.Loading) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading,
    )

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
  val filteredProducts = searchQuery
    .debounce(1000)
    .flatMapLatest { query ->
      if (query.isBlank()) {
        allProducts
      } else {
        adminRepository.searchProductByTitle(query)
          .map { UiState.Content(it) }
          .onStart<UiState<List<com.nutrisport.shared.domain.Product>>> { emit(UiState.Loading) }
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading,
    )
}
