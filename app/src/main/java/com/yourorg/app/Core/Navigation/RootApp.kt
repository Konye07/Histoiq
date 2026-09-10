package com.yourorg.app.Core.Navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Account.UserAccount
import com.yourorg.app.Data.Settings.SettingsStore
import com.yourorg.app.Features.Auth.AuthScreen

/**
 * Gates the main app behind sign-in: shows AuthScreen until a session exists or the player
 * chooses to continue as a guest, then AppNavHost either way — daily limits, scores, and stats
 * are stored per-device for guests (not synced), so guest play works identically except for friends.
 */
@Composable
fun RootApp() {
    val context = LocalContext.current
    var account by remember { mutableStateOf<UserAccount?>(null) }
    var isGuest by remember { mutableStateOf(SettingsStore.guestMode(context)) }
    var restoring by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        account = AccountRepository.restoreSession()
        restoring = false
    }

    when {
        restoring -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        account == null && !isGuest -> AuthScreen(
            onAuthenticated = { account = AccountRepository.currentAccount() },
            onContinueAsGuest = {
                SettingsStore.setGuestMode(context, true)
                isGuest = true
            }
        )
        else -> AppNavHost(onSignedOut = {
            SettingsStore.setGuestMode(context, false)
            account = null
            isGuest = false
        })
    }
}
