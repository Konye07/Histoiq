package com.konye.historiq.Core.Ads

import com.konye.historiq.Core.Platform.IosNativeBridges

/** Real implementation lives in Swift (iosApp/AdsBridge.swift, via the GoogleMobileAds SPM package). */
actual fun showRewardedAd(onRewarded: () -> Unit) {
    val ads = IosNativeBridges.ads ?: return
    ads.showRewarded(AdsConfig.REWARDED_UNIT_ID_IOS, onRewarded)
}
