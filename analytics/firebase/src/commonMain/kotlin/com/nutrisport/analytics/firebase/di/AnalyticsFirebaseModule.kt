package com.nutrisport.analytics.firebase.di

import com.nutrisport.analytics.firebase.FirebaseAnalyticsProcessor
import com.nutrisport.analytics.firebase.mapper.AuthFirebaseMapper
import com.nutrisport.analytics.firebase.mapper.EcommerceFirebaseMapper
import com.nutrisport.analytics.firebase.mapper.FirebaseMapperFacade
import com.nutrisport.analytics.firebase.mapper.NavigationFirebaseMapper
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.analytics
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val analyticsFirebaseModule = module {
  factoryOf(::NavigationFirebaseMapper)
  factoryOf(::AuthFirebaseMapper)
  factoryOf(::EcommerceFirebaseMapper)
  factoryOf(::FirebaseMapperFacade)
  factory {
    FirebaseAnalyticsProcessor(
      firebaseAnalytics = Firebase.analytics,
      mappers = get<FirebaseMapperFacade>().getMappers(),
    )
  }
}
