package com.portfolio.nutrisport

import com.nutrisport.navigation.debug.DebugToolkit
import org.koin.core.module.Module
import org.koin.dsl.module

object DebugModuleProvider {
  val modules: List<Module> = listOf(
    module { single<DebugToolkit> { TraceyDebugToolkit() } },
    module { single<FirebaseConfigurator> { DebugFirebaseConfigurator() } },
  )
}
