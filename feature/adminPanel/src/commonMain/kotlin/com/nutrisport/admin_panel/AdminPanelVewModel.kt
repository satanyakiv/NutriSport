package com.nutrisport.admin_panel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.data.domain.AdminRepository
import com.nutrisport.data.AdminRepositoryImpl
import com.nutrisport.shared.util.RequestState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class AdminPanelVewModel(
  private val adminRepository: AdminRepository = AdminRepositoryImpl(),
): ViewModel() {
  private val allProducts = adminRepository.readLastTenProducts()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = RequestState.Loading,
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
      if (query.isBlank()) allProducts else adminRepository.searchProductByTitle(query)
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = RequestState.Loading,
    )
}