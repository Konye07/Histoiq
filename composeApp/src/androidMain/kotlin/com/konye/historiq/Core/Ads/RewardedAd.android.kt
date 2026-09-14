package com.konye.historiq.Core.Ads

import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.konye.historiq.Core.Platform.AndroidPlatformContext

actual fun showRewardedAd(onRewarded: () -> Unit) {
    val activity = AndroidPlatformContext.currentActivity ?: return

    RewardedAd.load(
        activity,
        AdsConfig.REWARDED_UNIT_ID,
        AdRequest.Builder().build(),
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {}
                }
                ad.show(activity) { onRewarded() }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {}
        }
    )
}
