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
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google

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

    val linked = linkOrSignInWithIdToken(
        provider = Google,
        idToken = credential.idToken,
        rawNonce = rawNonce,
        onLinkFallback = { e ->
            if ((e as? AuthRestException)?.errorCode == AuthErrorCode.IdentityAlreadyExists) {
                Log.i(TAG, "This Google account already has an app account — signing in to it instead of linking")
            } else {
                Log.w(TAG, "Linking Google to the guest session failed, falling back to signInWith(IDToken): ${e::class.simpleName}: ${e.message}", e)
            }
        },
        onSignInFallbackFailed = { e ->
            Log.e(TAG, "Google sign-in failed: signInWith(IDToken) fallback also threw ${e::class.simpleName}: ${e.message}", e)
        }
    )
    if (!linked) return GoogleAuthOutcome.CancelledOrFailed

    return GoogleAuthOutcome.SignedIn(
        AuthResult(
            providerUserId = credential.uniqueId,
            email = credential.email ?: "",
            displayName = credential.displayName ?: credential.email ?: "Google",
            avatarUrl = credential.profilePictureUri?.toString()
        )
    )
}
