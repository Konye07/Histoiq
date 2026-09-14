package com.konye.historiq.Features.Auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Localization.StringsAuthSettings
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Account.AuthProviderType
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(onAuthenticated: () -> Unit, onContinueAsGuest: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var loadingProvider by remember { mutableStateOf<AuthProviderType?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val signInFailedMessage = StringsAuthSettings.auth_sign_in_failed

    fun continueWith(provider: AuthProviderType) {
        if (loadingProvider != null) return
        loadingProvider = provider
        errorMessage = null
        scope.launch {
            try {
                AccountRepository.continueWithProvider(provider)
                loadingProvider = null
                onAuthenticated()
            } catch (e: Exception) {
                loadingProvider = null
                errorMessage = signInFailedMessage
            }
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
                StringsAuthSettings.auth_title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                StringsAuthSettings.auth_subtitle,
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
                    Text(StringsAuthSettings.auth_continue_with_google)
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
                    Text(StringsAuthSettings.auth_continue_with_apple)
                }
            }

            errorMessage?.let { message ->
                Spacer(Modifier.height(12.dp))
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onContinueAsGuest,
                enabled = loadingProvider == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(StringsAuthSettings.auth_continue_without_login)
            }

            Spacer(Modifier.height(12.dp))

            Text(
                StringsAuthSettings.auth_guest_info,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            Text(
                StringsAuthSettings.auth_mock_notice,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
