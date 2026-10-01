package com.konye.historiq.Core.Ads

import com.konye.historiq.Core.Platform.IosNativeBridges

/** Real implementation lives in Swift (iosApp/ConsentBridge.swift, via the GoogleUserMessagingPlatform SPM package). */
actual fun requestConsentAndInitializeAds(onComplete: () -> Unit) {
    val consent = IosNativeBridges.consent ?: return onComplete()
    consent.requestConsentAndInitializeAds(onComplete)
}

actual fun showPrivacyOptionsForm(onComplete: () -> Unit) {
    val consent = IosNativeBridges.consent ?: return onComplete()
    consent.showPrivacyOptionsForm(onComplete)
}

actual fun isPrivacyOptionsRequired(): Boolean =
    IosNativeBridges.consent?.isPrivacyOptionsRequired() ?: false
