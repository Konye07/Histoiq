package com.yourorg.app.Core.Billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package
import com.yourorg.app.Data.Settings.SettingsStore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Wraps RevenueCat's Kotlin Multiplatform SDK (wraps Play Billing/StoreKit) for the ad-free
 * subscription. [configure] is a no-op — not an error — when the platform's API key is blank
 * (RevenueCatConfig, generated from local.properties; see build.gradle.kts): there's no RevenueCat
 * project yet, so every check below just reports "not subscribed" instead of crashing. Once real,
 * SettingsStore.hasAdFreeSubscription() (the flag Core/Ads/ResultsAdGate.kt already gates on) is
 * kept in sync with RevenueCat's own entitlement, not treated as the source of truth itself.
 */
object SubscriptionRepository {
    private var configured = false

    /** Call once at app start with a stable id — this app uses the same Supabase auth.uid() every
     * device already has (guest or signed-in; see AccountRepository.ensureAnonymousSession). */
    fun configure(appUserId: String) {
        val apiKey = revenueCatApiKey()
        if (apiKey.isBlank()) return
        Purchases.configure(apiKey = apiKey) {
            this.appUserId = appUserId
        }
        configured = true
    }

    /** Refreshes SettingsStore.hasAdFreeSubscription() from RevenueCat's real entitlement status. */
    suspend fun refreshEntitlement() {
        if (!configured) return
        val info = customerInfoOrNull() ?: return
        SettingsStore.setAdFreeSubscription(isAdFreeActive(info))
    }

    suspend fun currentOffering(): Offering? {
        if (!configured) return null
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.getOfferings(
                onError = { cont.resume(null) },
                onSuccess = { offerings -> cont.resume(offerings.current) }
            )
        }
    }

    suspend fun purchase(pkg: Package): PurchaseOutcome {
        if (!configured) return PurchaseOutcome.Failed("RevenueCat is not configured")
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.purchase(
                packageToPurchase = pkg,
                onError = { error, userCancelled ->
                    cont.resume(if (userCancelled) PurchaseOutcome.Cancelled else PurchaseOutcome.Failed(error.message))
                },
                onSuccess = { _, customerInfo ->
                    SettingsStore.setAdFreeSubscription(isAdFreeActive(customerInfo))
                    cont.resume(PurchaseOutcome.Success)
                }
            )
        }
    }

    /** True if the restore actually found and applied an active ad-free entitlement. */
    suspend fun restorePurchases(): Boolean {
        if (!configured) return false
        return suspendCancellableCoroutine { cont ->
            Purchases.sharedInstance.restorePurchases(
                onError = { cont.resume(false) },
                onSuccess = { customerInfo ->
                    val active = isAdFreeActive(customerInfo)
                    SettingsStore.setAdFreeSubscription(active)
                    cont.resume(active)
                }
            )
        }
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

sealed class PurchaseOutcome {
    object Success : PurchaseOutcome()
    object Cancelled : PurchaseOutcome()
    data class Failed(val message: String) : PurchaseOutcome()
}
