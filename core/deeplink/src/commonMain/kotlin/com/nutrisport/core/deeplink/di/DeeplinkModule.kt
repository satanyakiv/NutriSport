package com.nutrisport.core.deeplink.di

import com.nutrisport.core.deeplink.DeeplinkBridge
import com.nutrisport.core.deeplink.pending.InMemoryPendingDeeplinkStorage
import com.nutrisport.core.deeplink.registry.DeeplinkRegistry
import com.nutrisport.core.deeplink.resolver.DeeplinkResolver
import com.nutrisport.core.deeplink.resolver.DefaultDeeplinkResolver
import com.nutrisport.shared.domain.deeplink.PendingDeeplinkStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module for :core:deeplink. Resolver, pending storage, and bridge are all
 * application-singletons — the bridge owns a supervisor scope so a failing admin
 * check does not cancel future deeplink dispatches. [com.nutrisport.shared.domain.deeplink.DeeplinkAuthGate]
 * is bound by the network module (`FirebaseDeeplinkAuthGate`).
 */
val deeplinkModule: Module = module {
  single<DeeplinkResolver> { DefaultDeeplinkResolver(routes = DeeplinkRegistry.routes) }
  single<PendingDeeplinkStorage> { InMemoryPendingDeeplinkStorage() }
  single {
    DeeplinkBridge(
      resolver = get(),
      pending = get(),
      router = get(),
      authGate = get(),
      scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    )
  }
}
