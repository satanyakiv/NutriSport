package com.nutrisport.home.domain

import com.nutrisport.home.domain.CustomDrawerState.Closed
import com.nutrisport.home.domain.CustomDrawerState.Opened

enum class CustomDrawerState {
  Opened,
  Closed,
}

fun CustomDrawerState.isOpened() = this == Opened

fun CustomDrawerState.isClosed() = this == Closed

fun CustomDrawerState.opposite() = if (this == Opened) Closed else Opened
