package com.yourorg.app.Core.Ads

/**
 * Shows a rewarded video ad for the "watch ad for a hint" flow (Core/Play/Shared/HintButton.kt) —
 * a completely separate ad slot from [showInterstitialAd], with no ad-free-subscription bypass at
 * all: even a subscriber still watches this one, by design (see [showInterstitialAd]'s doc).
 * [onRewarded] fires only once the user actually earns the reward (finishes the video) — a failed
 * load/show, or an abandoned video, calls nothing and the hint simply doesn't reveal.
 */
expect fun showRewardedAd(onRewarded: () -> Unit)
