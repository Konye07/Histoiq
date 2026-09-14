package com.konye.historiq.Core.Navigation

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
import com.konye.historiq.Core.Billing.SubscriptionRepository
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Account.UserAccount
import com.konye.historiq.Data.Settings.SettingsStore
import com.konye.historiq.Features.Auth.AuthScreen

/**
 * Gates the main app behind sign-in: shows AuthScreen until a session exists or the player
 * chooses to continue as a guest, then AppNavHost either way — daily limits, scores, and stats
 * are stored per-device for guests (not synced), so guest play works identically except for friends.
 */
@Composable
fun RootApp() {
    var account by remember { mutableStateOf<UserAccount?>(null) }
    var isGuest by remember { mutableStateOf(SettingsStore.guestMode()) }
    var restoring by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        account = AccountRepository.restoreSession()
        restoring = false
    }

    // Runs concurrently with the effect above rather than gating `restoring` on it — a brand-new
    // device's ensureAnonymousSession() call is a network round-trip, and startup shouldn't wait on
    // subscription setup to show its first screen.
    LaunchedEffect(Unit) {
        val uid = AccountRepository.ensureAnonymousSession()
        SubscriptionRepository.configure(uid)
        SubscriptionRepository.refreshEntitlement()
    }

    when {
        restoring -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        account == null && !isGuest -> AuthScreen(
            onAuthenticated = { account = AccountRepository.currentAccount() },
            onContinueAsGuest = {
                SettingsStore.setGuestMode(true)
                isGuest = true
            }
        )
        else -> AppNavHost(onSignedOut = {
            SettingsStore.setGuestMode(false)
            account = null
            isGuest = false
        })
    }
}
