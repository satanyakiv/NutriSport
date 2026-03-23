package com.nutrisport.data

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.himanshoe.tracey.TraceyHost
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import com.nutrisport.navigation.SetupNavGraph
import com.nutrisport.shared.Constants
import com.nutrisport.shared.domain.CustomerRepository
import com.nutrisport.shared.navigation.Screen
import com.nutrisport.shared.util.AppConfig
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun AppContent() {
  MaterialTheme {
    val customerRepository = koinInject<CustomerRepository>()
    val isUserAuthenticated = remember { customerRepository.getCurrentUserId() != null }
    val startDestination = remember { if (isUserAuthenticated) Screen.HomeGraph else Screen.Auth }
    var appReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
      GoogleAuthProvider.create(
        credentials = GoogleAuthCredentials(serverId = Constants.GOOGLE_WEB_CLIENT_ID)
      )
      appReady = true
    }

    AnimatedVisibility(
      modifier = Modifier.fillMaxSize(),
      visible = appReady
    ) {
      if (AppConfig.isDebug) {
        TraceyHost {
          SetupNavGraph(startDestination = startDestination)
        }
      } else {
        SetupNavGraph(startDestination = startDestination)
      }
    }
  }
}
