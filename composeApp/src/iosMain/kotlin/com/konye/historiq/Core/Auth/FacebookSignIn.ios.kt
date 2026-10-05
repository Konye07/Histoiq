package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Remote.OAUTH_REDIRECT_HOST
import com.konye.historiq.Data.Remote.OAUTH_REDIRECT_SCHEME
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem

actual suspend fun signInWithFacebook(): FacebookAuthOutcome = awaitFacebookOAuthSession()

/**
 * A plain `object` (not a bare top-level function) specifically so Swift can call it reliably as
 * `FacebookOAuthBridge.shared.handleDeeplink(url: url)` — the same, already-proven Kotlin/Native
 * object-export convention [com.konye.historiq.Core.Platform.IosNativeBridges] uses, rather than
 * relying on Kotlin/Native's file-facade class-naming for a bare top-level function (untestable
 * without a Mac, so not worth the guess).
 */
object FacebookOAuthBridge {
    /** Called from iOSApp.swift's onOpenURL for every incoming URL — completes whichever OAuth
     * attempt (if any) is currently awaiting one. A URL that doesn't match the configured
     * deep-link scheme/host is a no-op call, see supabase-kt's own handleDeeplinks source.
     *
     * Checks for `error`/`error_code` query params itself (Supabase's standard OAuth-redirect
     * error convention, confirmed from supabase-kt's own UrlUtils.kt) *before* delegating to the
     * library's handleDeeplinks — that call only emits an auth event for them (its onError
     * callback covers a failed code exchange, not these), so this is the only way to distinguish
     * an explicit failure (e.g. `identity_already_exists`) from the user simply not completing the
     * flow. Query-item extraction mirrors supabase-kt's own Apple.kt private helper. */
    fun handleDeeplink(url: NSURL) {
        if (url.scheme == OAUTH_REDIRECT_SCHEME && url.host == OAUTH_REDIRECT_HOST) {
            deeplinkSeenForPendingAttempt = true
            val components = NSURLComponents(url, false)
            val errorCode = getQueryItem(components, "error_code") ?: getQueryItem(components, "error")
            if (errorCode != null) {
                pendingOAuthResult?.complete(OAuthRoundTripResult.ExplicitFailure)
                return
            }
        }
        SupabaseClient.client.handleDeeplinks(url, onSessionSuccess = { session ->
            pendingOAuthResult?.complete(OAuthRoundTripResult.Session(session))
        })
    }

    private fun getQueryItem(components: NSURLComponents, key: String): String? {
        return (components.queryItems?.firstOrNull { it is NSURLQueryItem && it.name == key } as? NSURLQueryItem)?.value
    }
}
