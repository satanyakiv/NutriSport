package com.nutrisport.products_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.Either
import com.nutrisport.shared.util.UiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ProductsOverviewViewModel(
  private val productRepository: ProductRepository,
) : ViewModel() {

  val products = combine(
    productRepository.readNewProducts(),
    productRepository.readDiscountedProducts(),
  ) { new, discounted ->
    when {
      new.isRight && discounted.isRight -> {
        val newList = (new as Either.Right).value
        val discountedList = (discounted as Either.Right).value
        val finalList = (newList + discountedList).distinctBy { it.id }
        UiState.Content(Either.Right(finalList))
      }
      new.isLeft -> UiState.Content(new)
      discounted.isLeft -> UiState.Content(discounted)
      else -> UiState.Loading
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)
}
