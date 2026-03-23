package com.nutrisport.analytics.firebase.mapper

import com.nutrisport.analytics.core.AnalyticsEventMapper

class FirebaseMapperFacade(
  private val navigationMapper: NavigationFirebaseMapper,
  private val authMapper: AuthFirebaseMapper,
  private val ecommerceMapper: EcommerceFirebaseMapper,
) {

  fun getMappers(): List<AnalyticsEventMapper> = listOf(
    navigationMapper,
    authMapper,
    ecommerceMapper,
  )
}
