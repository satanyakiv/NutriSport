package com.nutrisport.analytics.core.di

import com.nutrisport.analytics.core.DebugAnalyticsProcessor
import com.nutrisport.analytics.core.NutriSportAnalytics
import com.nutrisport.analytics.core.NutriSportAnalyticsImpl
import com.nutrisport.shared.domain.coroutine.CoroutineDispatcherProvider
import com.nutrisport.shared.domain.coroutine.DefaultCoroutineDispatcherProvider
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val analyticsCoreModule = module {
  single<CoroutineDispatcherProvider> { DefaultCoroutineDispatcherProvider }
  single<NutriSportAnalytics> { NutriSportAnalyticsImpl(get()) }
  factoryOf(::DebugAnalyticsProcessor)
}
