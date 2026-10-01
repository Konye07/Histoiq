package com.konye.historiq.Core.Ads

import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.konye.historiq.Core.Platform.AndroidPlatformContext

private const val TAG = "HistoriqConsent"

actual fun requestConsentAndInitializeAds(onComplete: () -> Unit) {
    val activity = AndroidPlatformContext.currentActivity
    if (activity == null) {
        onComplete()
        return
    }

    val paramsBuilder = ConsentRequestParameters.Builder()
    if (AdsConfig.DEBUG_GEO_EEA_TEST_DEVICE_ID.isNotBlank()) {
        paramsBuilder.setConsentDebugSettings(
            ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .addTestDeviceHashedId(AdsConfig.DEBUG_GEO_EEA_TEST_DEVICE_ID)
                .build()
        )
    }

    val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
    consentInformation.requestConsentInfoUpdate(
        activity,
        paramsBuilder.build(),
        {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                if (formError != null) {
                    Log.e(TAG, "Consent form error: code=${formError.errorCode} message=${formError.message}")
                }
                MobileAds.initialize(activity)
                onComplete()
            }
        },
        { formError ->
            Log.e(TAG, "Consent info update failed: code=${formError.errorCode} message=${formError.message}")
            MobileAds.initialize(activity)
            onComplete()
        }
    )
}

actual fun showPrivacyOptionsForm(onComplete: () -> Unit) {
    val activity = AndroidPlatformContext.currentActivity
    if (activity == null) {
        onComplete()
        return
    }
    UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
        if (formError != null) {
            Log.e(TAG, "Privacy options form error: code=${formError.errorCode} message=${formError.message}")
        }
        onComplete()
    }
}

actual fun isPrivacyOptionsRequired(): Boolean {
    val consentInformation = UserMessagingPlatform.getConsentInformation(AndroidPlatformContext.app)
    return consentInformation.privacyOptionsRequirementStatus == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
}
