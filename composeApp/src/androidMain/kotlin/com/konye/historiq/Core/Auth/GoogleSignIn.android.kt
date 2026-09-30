package com.konye.historiq.Core.Auth

import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.konye.historiq.Core.Platform.AndroidPlatformContext
import com.konye.historiq.Data.Account.AuthResult
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken

private const val TAG = "HistoriqAuth"

actual suspend fun signInWithGoogle(): GoogleAuthOutcome {
    val activity = AndroidPlatformContext.currentActivity ?: run {
        Log.e(TAG, "Google sign-in failed: no current activity")
        return GoogleAuthOutcome.CancelledOrFailed
    }

    val (rawNonce, hashedNonce) = generateNonce()
    val option = GetSignInWithGoogleOption.Builder(GoogleAuthConfig.WEB_CLIENT_ID)
        .setNonce(hashedNonce)
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

    val credential = try {
        val response = CredentialManager.create(activity).getCredential(request = request, context = activity)
        val raw = response.credential as? CustomCredential ?: run {
            Log.e(TAG, "Google sign-in failed: credential was not a CustomCredential (type=${response.credential.type})")
            return GoogleAuthOutcome.CancelledOrFailed
        }
        if (raw.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            Log.e(TAG, "Google sign-in failed: unexpected credential type ${raw.type}")
            return GoogleAuthOutcome.CancelledOrFailed
        }
        GoogleIdTokenCredential.createFrom(raw.data)
    } catch (e: GetCredentialException) {
        Log.e(TAG, "Google sign-in failed: getCredential threw ${e::class.simpleName} (type=${e.type}): ${e.message}", e)
        return GoogleAuthOutcome.CancelledOrFailed
    } catch (e: GoogleIdTokenParsingException) {
        Log.e(TAG, "Google sign-in failed: could not parse GoogleIdTokenCredential: ${e.message}", e)
        return GoogleAuthOutcome.CancelledOrFailed
    }

    val auth = SupabaseClient.client.auth
    try {
        // Links to the device's existing anonymous session (same auth.uid()) so a guest's prior
        // score_history/quiz_progress carries over, instead of starting a fresh account.
        auth.linkIdentityWithIdToken(provider = Google, idToken = credential.idToken) {
            nonce = rawNonce
        }
    } catch (e: Exception) {
        Log.w(TAG, "linkIdentityWithIdToken failed, falling back to signInWith(IDToken): ${e::class.simpleName}: ${e.message}", e)
        try {
            // Falls back to a plain sign-in if this Google account is already linked to a
            // different auth.uid() (e.g. the device signed out and is re-linking the same
            // account) — linking always fails in that case, signing in as that existing user
            // is the correct recovery, not a hard error.
            auth.signInWith(IDToken) {
                idToken = credential.idToken
                provider = Google
                nonce = rawNonce
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed: signInWith(IDToken) fallback also threw ${e::class.simpleName}: ${e.message}", e)
            return GoogleAuthOutcome.CancelledOrFailed
        }
    }

    return GoogleAuthOutcome.SignedIn(
        AuthResult(
            providerUserId = credential.uniqueId,
            email = credential.email ?: "",
            displayName = credential.displayName ?: credential.email ?: "Google",
            avatarUrl = credential.profilePictureUri?.toString()
        )
    )
}
