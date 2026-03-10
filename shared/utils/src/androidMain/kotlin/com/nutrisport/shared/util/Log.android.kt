package com.nutrisport.shared.util

actual fun identityHash(any: Any): Int = java.lang.System.identityHashCode(any)
