package com.konye.historiq.Core.Navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.konye.historiq.Core.Ads.requestConsentAndInitializeAds
import com.konye.historiq.Core.Theme.APP_LOGO_SHARED_KEY
import com.konye.historiq.Core.Theme.AppLogoImage
import com.konye.historiq.Core.Theme.LocalSharedTransitionScope
import com.konye.historiq.Data.Account.AccountRepository
import com.konye.historiq.Data.Account.UserAccount
import com.konye.historiq.Data.Settings.SettingsStore
import com.konye.historiq.Features.Auth.AuthScreen
import com.konye.historiq.Features.Onboarding.TutorialScreen

/**
 * Gates the main app behind sign-in: shows AuthScreen until a session exists or the player
 * chooses to continue as a guest, then the one-time TutorialScreen if it hasn't been seen or
 * skipped yet on this device, then AppNavHost — daily limits, scores, and stats are stored
 * per-device for guests (not synced), so guest play works identically except for friends.
 *
 * Wrapped in [SharedTransitionLayout] so the splash logo below can fly into place as PlayScreen's
 * docked header logo once loading finishes — see [LocalSharedTransitionScope] for how that scope
 * reaches PlayScreen, several navigation layers down inside [AppNavHost].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RootApp() {
    var account by remember { mutableStateOf<UserAccount?>(null) }
    var isGuest by remember { mutableStateOf(SettingsStore.guestMode()) }
    var restoring by remember { mutableStateOf(true) }
    var hasSeenTutorial by remember { mutableStateOf(SettingsStore.hasSeenTutorial()) }

    LaunchedEffect(Unit) {
        account = AccountRepository.restoreSession()
        restoring = false
    }

    SharedTransitionLayout(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            when {
                restoring -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    AppLogoImage(
                        modifier = Modifier
                            .size(140.dp)
                            .sharedElementWithCallerManagedVisibility(
                                rememberSharedContentState(key = APP_LOGO_SHARED_KEY),
                                visible = true,
                                clipInOverlayDuringTransition = OverlayClip(RectangleShape)
                            )
                    )
                }
                account == null && !isGuest -> AuthScreen(
                    onAuthenticated = { account = AccountRepository.currentAccount() },
                    onContinueAsGuest = {
                        SettingsStore.setGuestMode(true)
                        isGuest = true
                    }
                )
                !hasSeenTutorial -> TutorialScreen(onFinished = {
                    SettingsStore.setHasSeenTutorial(true)
                    hasSeenTutorial = true
                })
                else -> {
                    // Fire-and-forget: the EEA/UK consent form (and, on iOS, the App Tracking
                    // Transparency prompt) only matter for ads, which are already deferred well
                    // past this point in the app (see ResultsAdGate) — there's no reason to delay
                    // showing the main app while this resolves over the network.
                    LaunchedEffect(Unit) { requestConsentAndInitializeAds {} }
                    AppNavHost(onSignedOut = {
                        SettingsStore.setGuestMode(false)
                        account = null
                        isGuest = false
                    })
                }
            }
        }
    }
}
