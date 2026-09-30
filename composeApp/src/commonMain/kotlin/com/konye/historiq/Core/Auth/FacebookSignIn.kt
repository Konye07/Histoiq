package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Account.AuthResult

sealed class FacebookAuthOutcome {
    data class SignedIn(val result: AuthResult) : FacebookAuthOutcome()
    object CancelledOrFailed : FacebookAuthOutcome()
    object NotImplementedOnThisPlatform : FacebookAuthOutcome()
}

/**
 * Real "Continue with Facebook" on Android: the native Facebook Login SDK's Limited-Login flow
 * (requests an OIDC `AuthenticationToken`/id_token, not the classic Graph API access token), then
 * Supabase's `linkIdentityWithIdToken` — see FacebookSignIn.android.kt.
 *
 * iOS doesn't have a real implementation yet; its actual always returns
 * [FacebookAuthOutcome.NotImplementedOnThisPlatform].
 */
expect suspend fun signInWithFacebook(): FacebookAuthOutcome
