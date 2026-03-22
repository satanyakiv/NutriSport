package com.nutrisport.analytics.core.di

import com.nutrisport.analytics.core.DebugAnalyticsProcessor
import com.nutrisport.analytics.core.NutriSportAnalytics
import com.nutrisport.analytics.core.NutriSportAnalyticsImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val analyticsCoreModule = module {
  single<NutriSportAnalytics> { NutriSportAnalyticsImpl() }
  factoryOf(::DebugAnalyticsProcessor)
}
