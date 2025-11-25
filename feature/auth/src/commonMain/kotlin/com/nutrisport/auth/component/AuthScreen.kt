package com.nutrisport.auth.component

import ContentWithMessageBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nutrisport.shared.Alpha
import com.nutrisport.shared.BebasNeuFont
import com.nutrisport.shared.FontSize
import com.nutrisport.shared.TextSecondary
import org.jetbrains.compose.ui.tooling.preview.Preview
import rememberMessageBarState

@Composable
fun AuthScreen() {
    val messageBarState = rememberMessageBarState()
    val loadingState by remember { mutableStateOf(false) }

    Scaffold { padding ->
        ContentWithMessageBar(
            modifier = Modifier.padding(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            messageBarState = messageBarState,
            errorMaxLines = 2,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "NUTRISPORT",
                        textAlign = TextAlign.Center,
                        fontFamily = BebasNeuFont(),
                        fontSize = FontSize.EXTRA_LARGE,
                        color = TextSecondary,
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(Alpha.HALF),
                        text = "Sign in to continue",
                        textAlign = TextAlign.Center,
                        fontSize = FontSize.EXTRA_REGULAR,
                        color = TextSecondary,
                    )
                }
                GoogleButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    loading = loadingState,
                    onClicked = {

                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun AuthScreenPreview() {
    AuthScreen()
}