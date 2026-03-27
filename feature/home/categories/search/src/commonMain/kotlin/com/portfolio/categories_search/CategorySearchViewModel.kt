package com.portfolio.categories_search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.ProductCategory
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class CategorySearchViewModel(
  private val productRepository: ProductRepository,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val products = productRepository.readProductsByCategoryFlow(
    category = ProductCategory.valueOf(
      savedStateHandle.get<String>("category") ?: ProductCategory.Protein.name
    )
  ).map { UiState.Content(it) }
    .onStart<UiState<List<com.nutrisport.shared.domain.Product>>> { emit(UiState.Loading) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading
    )

  private var _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery

  fun updateSearchQuery(value: String) {
    _searchQuery.value = value
  }

  @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
  val filteredProducts = searchQuery
    .debounce(1000)
    .flatMapLatest { query ->
      if (query.isBlank()) {
        products
      } else {
        val currentData = products.value.getSuccessDataOrNull()
        if (currentData != null) {
          flowOf(
            UiState.Content(
              Either.Right(
                currentData.filter {
                  it.title.lowercase().contains(query.lowercase())
                }
              )
            )
          )
        } else {
          products
        }
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = UiState.Loading
    )
}
