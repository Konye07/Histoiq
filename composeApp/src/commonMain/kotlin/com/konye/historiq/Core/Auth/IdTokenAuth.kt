package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Quiz.GuestProgress
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.IDTokenProvider
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.CancellationException

/**
 * Links [idToken] to the device's current anonymous session (same auth.uid()) so a guest's prior
 * score_history/quiz_progress carries over; falls back to a plain sign-in if the link fails —
 * chiefly because this identity is already linked to a *different* existing account (a new phone, a
 * reinstall, or the device signed out and is re-linking the same Google/Apple account), where
 * signing in as that existing user is the correct recovery, not a hard error. That swaps the
 * session to the existing account, so the guest's results are read first and copied across
 * afterwards ([GuestProgress]), the account's own results winning wherever both have a game/day.
 * Shared by every real ID-token provider on every platform so this fallback dance only needs to be
 * gotten right once. [onLinkFallback] is invoked with the link failure: an identity_already_exists
 * error is the routine one, anything else means the guest was not linked and is worth a look.
 * [onSignInFallbackFailed] is invoked if the fallback sign-in *also* fails — the real error in
 * that case (Supabase rejects the token entirely: e.g. an unconfigured provider, an audience/
 * nonce mismatch, or a bad provider secret) is worth surfacing, unlike the expected link failure.
 * [accessToken] must accompany [idToken] whenever the provider issued one with it (Google on iOS):
 * such ID tokens carry an `at_hash` claim, and Supabase Auth logs a warning when the matching
 * access token is missing and has said it will require it in a future release.
 */
suspend fun linkOrSignInWithIdToken(
    provider: IDTokenProvider,
    idToken: String,
    rawNonce: String,
    accessToken: String? = null,
    onLinkFallback: (Throwable) -> Unit = {},
    onSignInFallbackFailed: (Throwable) -> Unit = {}
): Boolean {
    val auth = SupabaseClient.client.auth
    try {
        auth.linkIdentityWithIdToken(provider = provider, idToken = idToken) {
            nonce = rawNonce
            this.accessToken = accessToken
        }
        return true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onLinkFallback(e)
    }

    // A failed link leaves the guest's session in place, so its rows are still readable here — and
    // stop being readable the moment signInWith swaps in the existing account's session.
    val guest = GuestProgress.capture()
    try {
        auth.signInWith(IDToken) {
            this.idToken = idToken
            this.provider = provider
            nonce = rawNonce
            this.accessToken = accessToken
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        onSignInFallbackFailed(e)
        return false
    }
    auth.currentUserOrNull()?.id?.let { guest?.carryOverTo(it) }
    return true
}
