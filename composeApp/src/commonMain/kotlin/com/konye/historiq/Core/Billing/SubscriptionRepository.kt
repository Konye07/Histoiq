package com.konye.historiq.Core.Billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.konye.historiq.Data.Settings.SettingsStore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Wraps RevenueCat's Kotlin Multiplatform SDK (wraps Play Billing/StoreKit) for the ad-free
 * subscription. [configure] is a no-op — not an error — when the platform's API key is blank
 * (RevenueCatConfig, generated from local.properties; see build.gradle.kts): there's no RevenueCat
 * project yet, so every check below just reports "not subscribed" instead of crashing. Once real,
 * SettingsStore.hasAdFreeSubscription() (the flag Core/Ads/ResultsAdGate.kt already gates on) is
 * kept in sync with RevenueCat's own entitlement, not treated as the source of truth itself.
 *
 * Deliberately does not wrap purchase/offering/restore calls itself — the Settings screen presents
 * RevenueCat's own Paywall and CustomerCenter Compose screens (purchases-kmp-ui) for that, which
 * own the whole pick-a-plan/purchase/manage/cancel/restore flow already. [isConfigured] is what
 * gates whether the Settings screen can safely show either one at all: calling
 * Purchases.sharedInstance before [configure] succeeds throws ("no singleton instance").
 */
object SubscriptionRepository {
    var isConfigured = false
        private set

    /** Call once at app start with a stable id — this app uses the same Supabase auth.uid() every
     * device already has (guest or signed-in; see AccountRepository.ensureAnonymousSession). */
    fun configure(appUserId: String) {
        val apiKey = revenueCatApiKey()
        if (apiKey.isBlank()) return
        Purchases.configure(apiKey = apiKey) {
            this.appUserId = appUserId
        }
        isConfigured = true
    }

    /** Refreshes SettingsStore.hasAdFreeSubscription() from RevenueCat's real entitlement status —
     * call at app start and again whenever the Paywall/CustomerCenter screen is dismissed. */
    suspend fun refreshEntitlement() {
        if (!isConfigured) return
        val info = customerInfoOrNull() ?: return
        applyCustomerInfo(info)
    }

    /** Applies a CustomerInfo already handed to us by a Paywall/CustomerCenter callback, without a
     * redundant getCustomerInfo() round-trip. */
    fun applyCustomerInfo(info: CustomerInfo) {
        SettingsStore.setAdFreeSubscription(isAdFreeActive(info))
    }

    private suspend fun customerInfoOrNull(): CustomerInfo? = suspendCancellableCoroutine { cont ->
        Purchases.sharedInstance.getCustomerInfo(
            onError = { cont.resume(null) },
            onSuccess = { info -> cont.resume(info) }
        )
    }

    private fun isAdFreeActive(info: CustomerInfo): Boolean =
        info.entitlements[RevenueCatConfig.AD_FREE_ENTITLEMENT_ID]?.isActive == true
}
