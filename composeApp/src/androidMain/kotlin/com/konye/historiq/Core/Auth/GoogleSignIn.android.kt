package com.konye.historiq.Core.Auth

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
import java.security.MessageDigest
import java.security.SecureRandom

actual suspend fun signInWithGoogle(): GoogleAuthOutcome {
    val activity = AndroidPlatformContext.currentActivity ?: return GoogleAuthOutcome.CancelledOrFailed

    val (rawNonce, hashedNonce) = generateNonce()
    val option = GetSignInWithGoogleOption.Builder(GoogleAuthConfig.WEB_CLIENT_ID)
        .setNonce(hashedNonce)
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()

    val credential = try {
        val response = CredentialManager.create(activity).getCredential(request = request, context = activity)
        val raw = response.credential as? CustomCredential ?: return GoogleAuthOutcome.CancelledOrFailed
        if (raw.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) return GoogleAuthOutcome.CancelledOrFailed
        GoogleIdTokenCredential.createFrom(raw.data)
    } catch (e: GetCredentialException) {
        return GoogleAuthOutcome.CancelledOrFailed
    } catch (e: GoogleIdTokenParsingException) {
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

private fun generateNonce(): Pair<String, String> {
    val rawBytes = ByteArray(32)
    SecureRandom().nextBytes(rawBytes)
    val rawNonce = rawBytes.joinToString("") { "%02x".format(it) }
    val hashedNonce = MessageDigest.getInstance("SHA-256")
        .digest(rawNonce.toByteArray())
        .joinToString("") { "%02x".format(it) }
    return rawNonce to hashedNonce
}
