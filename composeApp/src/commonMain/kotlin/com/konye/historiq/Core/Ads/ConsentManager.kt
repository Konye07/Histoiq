package com.konye.historiq.Core.Ads

/**
 * Google's UMP consent flow (EEA/UK ad-personalization consent) followed by, on iOS only, the
 * App Tracking Transparency prompt — then initializes the Mobile Ads SDK. Always initializes ads
 * regardless of the consent outcome (Google's EU User Consent Policy still requires serving
 * non-personalized ads to users who decline); the GMA SDK itself restricts fill to non-personalized
 * ads based on the consent signal it already recorded, so [Core.Ads.InterstitialAd]/[Core.Ads.RewardedAd]
 * need no changes. Fire-and-forget: never blocks navigation, matching how ads are already deferred
 * past app launch (see [com.konye.historiq.Core.Ads.ResultsAdGate]).
 */
expect fun requestConsentAndInitializeAds(onComplete: () -> Unit)

/** Re-presents the EEA/UK "privacy options" form later, for a Settings screen entry point — required
 * so a user can change their ad-consent choice after the first launch, not just see it once. */
expect fun showPrivacyOptionsForm(onComplete: () -> Unit)

/** Whether a "Privacy choices" entry point should be shown at all — true only for EEA/UK users,
 * per the consent SDK's own cached state from the last [requestConsentAndInitializeAds] call. */
expect fun isPrivacyOptionsRequired(): Boolean
