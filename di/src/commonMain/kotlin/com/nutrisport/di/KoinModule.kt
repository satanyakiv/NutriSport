package com.nutrisport.di

import com.nutrisport.admin_panel.di.adminPanelModule
import com.nutrisport.analytics.core.di.analyticsCoreModule
import com.nutrisport.analytics.firebase.di.analyticsFirebaseModule
import com.nutrisport.auth.di.authModule
import com.nutrisport.cart.di.cartModule
import com.nutrisport.checkout.di.checkoutModule
import com.nutrisport.data.di.networkModule
import com.nutrisport.database.di.databaseModule
import com.nutrisport.details.di.detailsModule
import com.nutrisport.home.di.homeModule
import com.nutrisport.manage_product.di.manageProductModule
import com.nutrisport.navigation.debug.DebugToolkit
import com.nutrisport.navigation.debug.NoOpDebugToolkit
import com.nutrisport.products_overview.di.productsOverviewModule
import com.nutrisport.profile.di.profileModule
import com.nutrisport.shared.domain.di.domainModule
import com.portfolio.categories_search.di.categorySearchModule
import com.portfolio.payment_completed.di.paymentModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

expect val targetModule: Module

private val debugToolkitFallback = module {
  single<DebugToolkit> { NoOpDebugToolkit() }
}

fun initializeKoin(
  useFakeData: Boolean = false,
  additionalModules: List<Module> = emptyList(),
  config: (KoinApplication.() -> Unit)? = null,
) {
  startKoin {
    config?.invoke(this)
    modules(
      buildList {
        add(debugToolkitFallback)
        addAll(additionalModules)
        add(targetModule)
        add(analyticsCoreModule)
        add(domainModule)
        if (useFakeData) {
          add(fakeNetworkModule)
        } else {
          add(databaseModule)
          add(networkModule)
          add(analyticsFirebaseModule)
        }
        add(authModule)
        add(homeModule)
        add(profileModule)
        add(detailsModule)
        add(productsOverviewModule)
        add(cartModule)
        add(checkoutModule)
        add(paymentModule)
        add(categorySearchModule)
        add(adminPanelModule)
        add(manageProductModule)
      },
    )
  }
}
