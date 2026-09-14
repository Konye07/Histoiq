package com.yourorg.app.Core.Platform

import android.app.Activity
import android.app.Application

/**
 * Populated once in [com.yourorg.app.MainApplication.onCreate] so Android `actual` implementations (settings
 * storage, haptics, ...) can reach a Context without threading one through every commonMain call
 * site — iOS needs no equivalent, since NSUserDefaults/UIFeedbackGenerator/etc. don't need an
 * app-level handle passed in.
 */
internal object AndroidPlatformContext {
    lateinit var app: Application

    /**
     * Kept in sync with [com.yourorg.app.Shared.MainActivity]'s lifecycle (set in onCreate, cleared
     * in onDestroy) — this app is single-Activity, so there's at most one to track. AdMob's
     * InterstitialAd/RewardedAd.show() needs a live Activity, not just an Application Context.
     */
    var currentActivity: Activity? = null
}
