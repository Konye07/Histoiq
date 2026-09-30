package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.IDTokenProvider
import io.github.jan.supabase.auth.providers.builtin.IDToken

/**
 * Links [idToken] to the device's current anonymous session (same auth.uid()) so a guest's prior
 * score_history/quiz_progress carries over; falls back to a plain sign-in if this identity is
 * already linked to a *different* existing account (e.g. the device signed out and is re-linking
 * the same Google/Apple account) — linking always fails in that case, signing in as that existing
 * user is the correct recovery, not a hard error. Shared by every real ID-token provider on every
 * platform so this fallback dance only needs to be gotten right once. [onLinkFallback] is invoked
 * with a diagnostic message only when the (expected, often harmless) link failure occurs.
 * [onSignInFallbackFailed] is invoked if the fallback sign-in *also* fails — the real error in
 * that case (Supabase rejects the token entirely: e.g. an unconfigured provider, an audience/
 * nonce mismatch, or a bad provider secret) is worth surfacing, unlike the expected link failure.
 */
suspend fun linkOrSignInWithIdToken(
    provider: IDTokenProvider,
    idToken: String,
    rawNonce: String,
    onLinkFallback: (Throwable) -> Unit = {},
    onSignInFallbackFailed: (Throwable) -> Unit = {}
): Boolean {
    val auth = SupabaseClient.client.auth
    try {
        auth.linkIdentityWithIdToken(provider = provider, idToken = idToken) {
            nonce = rawNonce
        }
        return true
    } catch (e: Exception) {
        onLinkFallback(e)
        return try {
            auth.signInWith(IDToken) {
                this.idToken = idToken
                this.provider = provider
                nonce = rawNonce
            }
            true
        } catch (e: Exception) {
            onSignInFallbackFailed(e)
            false
        }
    }
}
