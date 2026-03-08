package com.nutrisport.products_overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nutrisport.shared.domain.ProductRepository
import com.nutrisport.shared.util.RequestState
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
      new.isSuccess() && discounted.isSuccess() -> {
        val finalList = (new.getSuccessData() + discounted.getSuccessData()).distinctBy { it.id }
        RequestState.Success(finalList)
      }
      new.isError() -> new
      discounted.isError() -> discounted
      else -> {
        RequestState.Loading
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RequestState.Loading)
}