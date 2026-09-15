package com.konye.historiq.Core.Ads

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.konye.historiq.Core.Platform.AndroidPlatformContext

private const val TAG = "HistoriqAds"

/**
 * Unlike showInterstitialAd, a load/show failure here must NOT call [onRewarded] — the caller
 * (HintAction) only reveals the hint once the reward is actually earned, so a silent failure here
 * just means the hint never appears, with nothing else visibly broken. Logged at error level so a
 * real cause (wrong ad unit ID/format, no fill, policy issue) shows up in Logcat instead of just
 * "the hint button doesn't do anything."
 */
actual fun showRewardedAd(onRewarded: () -> Unit) {
    val activity = AndroidPlatformContext.currentActivity ?: return

    RewardedAd.load(
        activity,
        AdsConfig.REWARDED_UNIT_ID,
        AdRequest.Builder().build(),
        object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e(TAG, "Rewarded ad failed to show: code=${adError.code} domain=${adError.domain} message=${adError.message}")
                    }
                }
                ad.show(activity) {
                    Log.d(TAG, "Rewarded ad: user earned the reward")
                    onRewarded()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e(TAG, "Rewarded ad failed to load: code=${error.code} domain=${error.domain} message=${error.message}")
            }
        }
    )
}
