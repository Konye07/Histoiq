package com.yourorg.app.Core.Ads

actual fun showInterstitialAd(onDismissed: () -> Unit) {
    onDismissed()
}
