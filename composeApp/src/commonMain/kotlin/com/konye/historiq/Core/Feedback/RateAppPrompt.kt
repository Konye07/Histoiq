package com.konye.historiq.Core.Feedback

import com.konye.historiq.Data.Settings.SettingsStore
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

/**
 * A plain custom "rate us" nudge — not Google/Apple's own native in-app-review API, just a dialog
 * linking out to the Play Store listing — gated so it can't nag: never again once the player rates
 * or declines for good, and at most once every [COOLDOWN_AFTER_LATER] before that, with an initial
 * delay so a brand-new install isn't asked on day one.
 */
@OptIn(ExperimentalTime::class)
object RateAppPrompt {
    private val INITIAL_DELAY = 3.days
    private val COOLDOWN_AFTER_LATER = 14.days
    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.konye.historiq"

    fun shouldShow(): Boolean {
        if (SettingsStore.rateAppDecided()) return false
        val now = Clock.System.now().toEpochMilliseconds()
        val nextEligibleAt = SettingsStore.rateAppNextEligibleAt()
        if (nextEligibleAt == null) {
            // First time this has ever been checked (e.g. a fresh install) — schedule the first
            // possible ask instead of showing immediately, and don't show this time either.
            SettingsStore.setRateAppNextEligibleAt(now + INITIAL_DELAY.inWholeMilliseconds)
            return false
        }
        return now >= nextEligibleAt
    }

    fun storeUrl(): String = PLAY_STORE_URL

    /** "Rate Now" and "No Thanks" both mean never ask again — only "Maybe Later" reschedules. */
    fun onDecided() {
        SettingsStore.setRateAppDecided(true)
    }

    fun onLater() {
        val now = Clock.System.now().toEpochMilliseconds()
        SettingsStore.setRateAppNextEligibleAt(now + COOLDOWN_AFTER_LATER.inWholeMilliseconds)
    }
}
