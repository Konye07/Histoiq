package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Account.AuthResult
import io.github.jan.supabase.auth.providers.Apple
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.Foundation.NSError
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * Held at file scope rather than as locals inside [requestAppleIdCredential]: ASAuthorizationController
 * only keeps a *weak* reference to its delegate/presentationContextProvider (mirroring Apple's own
 * documented sample code, which holds these on the presenting view controller for the same reason)
 * — without a strong reference surviving until the async callback fires, Kotlin/Native's ARC would
 * deallocate them first and the callback would silently never arrive.
 */
private var retainedDelegate: ASAuthorizationControllerDelegateProtocol? = null
private var retainedPresentationProvider: ASAuthorizationControllerPresentationContextProvidingProtocol? = null

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual suspend fun signInWithApple(): AppleAuthOutcome {
    val (rawNonce, hashedNonce) = generateNonce()

    val credential = requestAppleIdCredential(hashedNonce) ?: return AppleAuthOutcome.CancelledOrFailed
    val tokenData = credential.identityToken ?: return AppleAuthOutcome.CancelledOrFailed
    val idToken = NSString.create(data = tokenData, encoding = NSUTF8StringEncoding)?.toString()
        ?: return AppleAuthOutcome.CancelledOrFailed

    val linked = linkOrSignInWithIdToken(provider = Apple, idToken = idToken, rawNonce = rawNonce)
    if (!linked) return AppleAuthOutcome.CancelledOrFailed

    // Apple only ever sends fullName/email on the very first authorization for this app — a
    // returning user's credential has both as null, which is fine here since that first sign-in
    // already created the profile row (falling back to the email, then a generic label, avoids an
    // empty display name on that normal repeat-sign-in path).
    val fullName = credential.fullName
    val displayName = listOfNotNull(fullName?.givenName, fullName?.familyName)
        .joinToString(" ")
        .ifBlank { credential.email ?: "Apple" }

    return AppleAuthOutcome.SignedIn(
        AuthResult(
            providerUserId = credential.user,
            email = credential.email ?: "",
            displayName = displayName,
            avatarUrl = null
        )
    )
}

@OptIn(ExperimentalForeignApi::class)
private suspend fun requestAppleIdCredential(hashedNonce: String): ASAuthorizationAppleIDCredential? =
    suspendCancellableCoroutine { continuation ->
        fun finish(result: ASAuthorizationAppleIDCredential?) {
            retainedDelegate = null
            retainedPresentationProvider = null
            if (continuation.isActive) continuation.resume(result)
        }

        val request = ASAuthorizationAppleIDProvider().createRequest().apply {
            requestedScopes = listOf(ASAuthorizationScopeFullName, ASAuthorizationScopeEmail)
            nonce = hashedNonce
        }

        val delegate = object : NSObject(), ASAuthorizationControllerDelegateProtocol {
            override fun authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization: ASAuthorization) {
                finish(didCompleteWithAuthorization.credential as? ASAuthorizationAppleIDCredential)
            }

            override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
                finish(null)
            }
        }
        val presentationProvider = object : NSObject(), ASAuthorizationControllerPresentationContextProvidingProtocol {
            override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): UIWindow? =
                UIApplication.sharedApplication.keyWindow
        }
        retainedDelegate = delegate
        retainedPresentationProvider = presentationProvider

        val controller = ASAuthorizationController(authorizationRequests = listOf(request))
        controller.delegate = delegate
        controller.presentationContextProvider = presentationProvider
        continuation.invokeOnCancellation {
            retainedDelegate = null
            retainedPresentationProvider = null
        }
        controller.performRequests()
    }
