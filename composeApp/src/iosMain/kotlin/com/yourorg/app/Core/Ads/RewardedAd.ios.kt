package com.yourorg.app.Core.Ads

actual fun showRewardedAd(onRewarded: () -> Unit) {
    onRewarded()
}
