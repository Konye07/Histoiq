package com.yourorg.app.Core.Navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Account.UserAccount
import com.yourorg.app.Features.Auth.AuthScreen

/**
 * Gates the main app behind sign-in: shows AuthScreen until a session exists or the player
 * chooses to continue as a guest, then AppNavHost either way — daily limits, scores, and stats
 * are stored per-device (not per-account), so guest play works identically except for friends.
 */
@Composable
fun RootApp() {
    val context = LocalContext.current
    var account by remember { mutableStateOf<UserAccount?>(AccountRepository.currentAccount(context)) }
    var isGuest by remember { mutableStateOf(false) }

    if (account == null && !isGuest) {
        AuthScreen(
            onAuthenticated = { account = AccountRepository.currentAccount(context) },
            onContinueAsGuest = { isGuest = true }
        )
    } else {
        AppNavHost(onSignedOut = { account = null; isGuest = false })
    }
}
