package com.konye.historiq.Core.Ads

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.konye.historiq.Core.Platform.AndroidPlatformContext

private const val TAG = "HistoriqAds"

actual fun showInterstitialAd(onDismissed: () -> Unit) {
    val activity = AndroidPlatformContext.currentActivity
    if (activity == null) {
        onDismissed()
        return
    }

    var dismissedOnce = false
    fun dismissOnce() {
        if (dismissedOnce) return
        dismissedOnce = true
        onDismissed()
    }

    InterstitialAd.load(
        activity,
        AdsConfig.INTERSTITIAL_UNIT_ID,
        AdRequest.Builder().build(),
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() = dismissOnce()
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(TAG, "Interstitial ad failed to show: code=${adError.code} domain=${adError.domain} message=${adError.message}")
                        dismissOnce()
                    }
                }
                ad.show(activity)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Interstitial ad failed to load: code=${error.code} domain=${error.domain} message=${error.message}")
                dismissOnce()
            }
        }
    )
}
