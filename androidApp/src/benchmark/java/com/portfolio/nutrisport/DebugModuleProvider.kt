package com.portfolio.nutrisport

import com.nutrisport.navigation.debug.DebugToolkit
import com.nutrisport.navigation.debug.NoOpDebugToolkit
import org.koin.core.module.Module
import org.koin.dsl.module

object DebugModuleProvider {
  val modules: List<Module> = listOf(
    module { single<DebugToolkit> { NoOpDebugToolkit() } },
    module { single<FirebaseConfigurator> { NoOpFirebaseConfigurator() } },
  )
}
