package com.konye.historiq.Data.Settings

import com.konye.historiq.Core.Platform.createSettings

/** User-facing app preferences: language, haptics, daily reminder. */
object SettingsStore {
    private val settings = createSettings("app_settings")

    private const val KEY_LANGUAGE = "language"
    private const val KEY_HAPTICS_ENABLED = "hapticsEnabled"
    private const val KEY_DAILY_REMINDER_ENABLED = "dailyReminderEnabled"
    private const val KEY_GUEST_MODE = "guestMode"
    private const val KEY_AD_FREE_SUBSCRIPTION = "adFreeSubscription"
    private const val KEY_DARK_MODE_ENABLED = "darkModeEnabled"
    private const val KEY_HAS_SEEN_TUTORIAL = "hasSeenTutorial"
    private const val KEY_RATE_APP_DECIDED = "rateAppDecided"
    private const val KEY_RATE_APP_NEXT_ELIGIBLE_AT = "rateAppNextEligibleAt"

    const val LANGUAGE_ENGLISH = "en"

    fun language(): String = settings.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH)

    fun setLanguage(language: String) {
        settings.putString(KEY_LANGUAGE, language)
    }

    /** Survives process death (e.g. a language change), unlike RootApp's in-memory `isGuest`. */
    fun guestMode(): Boolean = settings.getBoolean(KEY_GUEST_MODE, false)

    fun setGuestMode(enabled: Boolean) {
        settings.putBoolean(KEY_GUEST_MODE, enabled)
    }

    fun hapticsEnabled(): Boolean = settings.getBoolean(KEY_HAPTICS_ENABLED, true)

    fun setHapticsEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_HAPTICS_ENABLED, enabled)
    }

    fun dailyReminderEnabled(): Boolean = settings.getBoolean(KEY_DAILY_REMINDER_ENABLED, false)

    fun setDailyReminderEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled)
    }

    /** No real payment/subscription backend yet (see Core/Ads/InterstitialAd.kt) — this flag is
     * the seam a future Play Billing/StoreKit integration will set once purchased. */
    fun hasAdFreeSubscription(): Boolean = settings.getBoolean(KEY_AD_FREE_SUBSCRIPTION, false)

    fun setAdFreeSubscription(enabled: Boolean) {
        settings.putBoolean(KEY_AD_FREE_SUBSCRIPTION, enabled)
    }

    /** Null means the user has never overridden it — App.kt falls back to `isSystemInDarkTheme()`
     * in that case, and keeps following the system live rather than freezing a one-time reading. */
    fun darkModeOverride(): Boolean? = settings.getBooleanOrNull(KEY_DARK_MODE_ENABLED)

    fun setDarkModeOverride(enabled: Boolean) {
        settings.putBoolean(KEY_DARK_MODE_ENABLED, enabled)
    }

    /** Gates the one-time onboarding tutorial in RootApp — false only until it's first
     * completed or skipped, on whichever device that happens on (not synced). */
    fun hasSeenTutorial(): Boolean = settings.getBoolean(KEY_HAS_SEEN_TUTORIAL, false)

    fun setHasSeenTutorial(seen: Boolean) {
        settings.putBoolean(KEY_HAS_SEEN_TUTORIAL, seen)
    }

    /** True once the player has rated or permanently declined — see RateAppPrompt, the one place
     * that reads/writes these two keys together. */
    fun rateAppDecided(): Boolean = settings.getBoolean(KEY_RATE_APP_DECIDED, false)

    fun setRateAppDecided(decided: Boolean) {
        settings.putBoolean(KEY_RATE_APP_DECIDED, decided)
    }

    /** Epoch millis of the next time the rate-app prompt is allowed to show; null means it's never
     * been scheduled yet (first app open). */
    fun rateAppNextEligibleAt(): Long? = settings.getLongOrNull(KEY_RATE_APP_NEXT_ELIGIBLE_AT)

    fun setRateAppNextEligibleAt(epochMillis: Long) {
        settings.putLong(KEY_RATE_APP_NEXT_ELIGIBLE_AT, epochMillis)
    }
}
