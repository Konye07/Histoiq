package com.yourorg.app.Features.Auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Account.AuthProviderType
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthenticated: () -> Unit, onContinueAsGuest: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loadingProvider by remember { mutableStateOf<AuthProviderType?>(null) }

    fun continueWith(provider: AuthProviderType) {
        if (loadingProvider != null) return
        loadingProvider = provider
        scope.launch {
            AccountRepository.continueWithProvider(context, provider)
            loadingProvider = null
            onAuthenticated()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Today in History",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Sign in to track your daily scores",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { continueWith(AuthProviderType.GOOGLE) },
                enabled = loadingProvider == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loadingProvider == AuthProviderType.GOOGLE) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continue with Google")
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = { continueWith(AuthProviderType.APPLE) },
                enabled = loadingProvider == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loadingProvider == AuthProviderType.APPLE) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Continue with Apple")
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onContinueAsGuest,
                enabled = loadingProvider == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue without login")
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "Without an account, your scores and stats still count and the daily limit still applies — you just won't have a friend code to share, or be able to sync across devices.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Text(
                "Mock sign-in for now — creates a local test account. Real Google/Apple sign-in needs OAuth credentials that aren't set up yet.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
