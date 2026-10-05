package com.konye.historiq.Core.Platform

/**
 * Google's Sign-In and Mobile Ads SDKs are distributed as Swift packages with no C/Objective-C
 * module map Kotlin/Native's cinterop can bind against directly (unlike first-party frameworks
 * such as AuthenticationServices) — so the real calls live in Swift (under iosApp) and are
 * exposed back to Kotlin through these plain callback-based interfaces. Kotlin/Native exports
 * public interfaces from the ComposeApp framework as Objective-C protocols automatically, which is
 * what lets a Swift class conform to them directly (`class GoogleSignInBridgeImpl: GoogleSignInBridge`).
 * Each bridge is registered once at app launch (iOSApp.swift's init) into [IosNativeBridges].
 */
interface GoogleSignInBridge {
    fun signIn(hashedNonce: String, onResult: (idToken: String?, accessToken: String?, uniqueId: String?, email: String?, displayName: String?, avatarUrl: String?) -> Unit)
}

interface AdsBridge {
    fun showInterstitial(adUnitId: String, onDismissed: () -> Unit)
    fun showRewarded(adUnitId: String, onRewarded: () -> Unit)
}

interface ConsentBridge {
    fun requestConsentAndInitializeAds(onComplete: () -> Unit)
    fun showPrivacyOptionsForm(onComplete: () -> Unit)
    fun isPrivacyOptionsRequired(): Boolean
}

object IosNativeBridges {
    var googleSignIn: GoogleSignInBridge? = null
    var ads: AdsBridge? = null
    var consent: ConsentBridge? = null
}
