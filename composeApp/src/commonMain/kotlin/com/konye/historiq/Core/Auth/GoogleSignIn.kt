package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Account.AuthResult

sealed class GoogleAuthOutcome {
    data class SignedIn(val result: AuthResult) : GoogleAuthOutcome()

    /** The user backed out of the account picker, or a real error occurred (no Play Services,
     * network failure, Supabase rejected the token) — never fall back to mock sign-in for this,
     * since the user made a real attempt and it didn't complete. */
    object CancelledOrFailed : GoogleAuthOutcome()

    /** Real Google Sign-In isn't wired up on this platform yet — the caller should fall back to
     * [com.konye.historiq.Data.Account.mockSignIn], same as before this existed. */
    object NotImplementedOnThisPlatform : GoogleAuthOutcome()
}

/**
 * Real "Continue with Google" on Android: Credential Manager + Google Identity Services for the
 * account picker/ID token, then Supabase's `linkIdentityWithIdToken` to attach it to the device's
 * existing anonymous session (same auth.uid(), so a guest's prior score_history/quiz_progress
 * carries over instead of starting a fresh account) — see GoogleSignIn.android.kt.
 *
 * iOS has no real implementation yet (no Apple-side OAuth client requested/set up); its actual
 * always returns [GoogleAuthOutcome.NotImplementedOnThisPlatform].
 */
expect suspend fun signInWithGoogle(): GoogleAuthOutcome
