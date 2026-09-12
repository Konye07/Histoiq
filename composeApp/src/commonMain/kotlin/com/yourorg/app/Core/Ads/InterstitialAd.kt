package com.yourorg.app.Core.Ads

/**
 * Shows a post-game interstitial ad and calls [onDismissed] once it's gone — including when no ad
 * was available to show (a network hiccup or empty fill must never block the results screen).
 * Callers must skip this call entirely for a subscriber (see SettingsStore.hasAdFreeSubscription)
 * rather than passing a flag in, since the rewarded "watch ad for a hint" flow (Core/Play/Shared/
 * HintButton.kt) is a completely separate ad slot with no subscription bypass at all.
 *
 * No real ad SDK wired in yet (same mock-now pattern as HintAction) — the actual bodies just call
 * [onDismissed] immediately. Swap in AdMob (or whichever network) per-platform here once there's a
 * real ad account/unit IDs to point at.
 */
expect fun showInterstitialAd(onDismissed: () -> Unit)
