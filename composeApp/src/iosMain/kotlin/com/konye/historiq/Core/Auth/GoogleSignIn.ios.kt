package com.konye.historiq.Core.Auth

import com.konye.historiq.Core.Platform.IosNativeBridges
import com.konye.historiq.Data.Account.AuthResult
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Real implementation lives in Swift (iosApp/GoogleSignInBridge.swift, via the GoogleSignIn SPM
 * package) — see [IosNativeBridges] for why Kotlin can't call the SDK directly. */
actual suspend fun signInWithGoogle(): GoogleAuthOutcome {
    val bridge = IosNativeBridges.googleSignIn ?: return GoogleAuthOutcome.CancelledOrFailed
    val (rawNonce, hashedNonce) = generateNonce()

    val result = suspendCancellableCoroutine { continuation ->
        bridge.signIn(hashedNonce) { idToken, accessToken, uniqueId, email, displayName, avatarUrl ->
            if (continuation.isActive) {
                continuation.resume(GoogleSignInResult(idToken, accessToken, uniqueId, email, displayName, avatarUrl))
            }
        }
    }
    val idToken = result.idToken
    val uniqueId = result.uniqueId
    if (idToken == null || uniqueId == null) return GoogleAuthOutcome.CancelledOrFailed

    val linked = linkOrSignInWithIdToken(
        provider = Google,
        idToken = idToken,
        rawNonce = rawNonce,
        accessToken = result.accessToken
    )
    if (!linked) return GoogleAuthOutcome.CancelledOrFailed

    return GoogleAuthOutcome.SignedIn(
        AuthResult(
            providerUserId = uniqueId,
            email = result.email ?: "",
            displayName = result.displayName ?: result.email ?: "Google",
            avatarUrl = result.avatarUrl
        )
    )
}

private data class GoogleSignInResult(
    val idToken: String?,
    val accessToken: String?,
    val uniqueId: String?,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?
)
