package com.yourorg.app.Core.Ads

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
import com.yourorg.app.Data.Settings.SettingsStore

/**
 * Wraps a game's results composable so a post-game interstitial plays first, unless the player has
 * an ad-free subscription ([SettingsStore.hasAdFreeSubscription]) — in which case the ad call is
 * skipped entirely, never just silently muted. Only call this on the transition that *just*
 * finished a round; when resuming into an already-completed one, call [results] directly instead
 * (each of the six game screens tracks that itself with a `resumedAlreadyDone` flag) — otherwise
 * every later visit to view past results would re-trigger the ad, which is exactly the excessive-
 * interstitial pattern ad networks penalize.
 */
@Composable
fun AdGatedResults(results: @Composable () -> Unit) {
    var adDone by remember { mutableStateOf(SettingsStore.hasAdFreeSubscription()) }

    LaunchedEffect(Unit) {
        if (!SettingsStore.hasAdFreeSubscription()) {
            showInterstitialAd { adDone = true }
        }
    }

    if (adDone) {
        results()
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
