package com.nutrisport.shared.domain

import com.nutrisport.shared.Resources
import org.jetbrains.compose.resources.DrawableResource

val Country.flag: DrawableResource
    get() = when (this) {
        Country.Serbia -> Resources.Flag.Serbia
        Country.India -> Resources.Flag.India
        Country.Usa -> Resources.Flag.Usa
    }
