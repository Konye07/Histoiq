package com.konye.historiq.Core.Ads

import com.konye.historiq.Core.Platform.IosNativeBridges

/** Real implementation lives in Swift (iosApp/AdsBridge.swift, via the GoogleMobileAds SPM package). */
actual fun showInterstitialAd(onDismissed: () -> Unit) {
    val ads = IosNativeBridges.ads ?: return onDismissed()
    ads.showInterstitial(AdsConfig.INTERSTITIAL_UNIT_ID_IOS, onDismissed)
}
