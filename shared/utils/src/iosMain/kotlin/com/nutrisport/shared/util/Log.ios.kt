package com.nutrisport.shared.util

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.identityHashCode
@OptIn(ExperimentalNativeApi::class)
actual fun identityHash(any: Any): Int = any.identityHashCode()
