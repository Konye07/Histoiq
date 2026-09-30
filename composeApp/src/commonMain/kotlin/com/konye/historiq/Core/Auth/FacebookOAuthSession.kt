package com.konye.historiq.Core.Auth

import com.konye.historiq.Data.Account.AuthResult
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

private const val OAUTH_TIMEOUT_MS = 120_000L

internal sealed class OAuthRoundTripResult {
    data class Session(val session: UserSession) : OAuthRoundTripResult()

    /** The redirect carried an explicit `error`/`error_code` param (most commonly
     * `identity_already_exists` — this Facebook identity is already linked to a different,
     * existing account), or the initial API call threw before a browser even opened (e.g. Manual
     * Linking isn't enabled in this Supabase project's Auth settings) — a real, known outcome,
     * never conflated with the user simply walking away without finishing. */
    object ExplicitFailure : OAuthRoundTripResult()
}

/**
 * Shared by both platforms' `signInWithFacebook()` actual — see FacebookSignIn.kt for why Facebook
 * specifically uses this browser-redirect OAuth flow instead of the native id-token pattern
 * Google/Apple use (facebook/facebook-android-sdk#1132: Facebook's Android SDK only mints a real
 * id-token on an account's first-ever authorization, an unfixed SDK limitation).
 *
 * Neither [io.github.jan.supabase.auth.Auth.signInWith] nor `linkIdentity` wait for the OAuth
 * round trip to finish — they only open the browser and return (confirmed by reading supabase-kt's
 * own source: the deep link's code exchange runs in the Auth plugin's own internal coroutine
 * scope, decoupled from that call). So this suspends on [pendingOAuthResult] instead, which the
 * platform's own deep-link handler (Android: MainActivity.onCreate/onNewIntent; iOS: iOSApp.swift's
 * onOpenURL) completes via `handleFacebookOAuthDeeplink`.
 */
internal var pendingOAuthResult: CompletableDeferred<OAuthRoundTripResult?>? = null

/**
 * Android-only: whether a deep link matching this attempt's scheme/host has been seen yet — see
 * `notifyFacebookOAuthResumed` in FacebookSignIn.android.kt for why racing a fixed grace period
 * against the deep link's own async code exchange is wrong once one has actually arrived.
 */
internal var deeplinkSeenForPendingAttempt: Boolean = false

/**
 * Tries linking Facebook to the current (guest or signed-in) session first — this is what
 * preserves a guest's same-device progress on a first-ever Facebook sign-in, mirroring Google/
 * Apple's linkIdentityWithIdToken-then-signInWith(IDToken) pattern (see IdTokenAuth.kt) — and
 * falls back to a plain sign-in only on an *explicit* link failure (most commonly a returning
 * user's second device/reinstall, where this identity already belongs to a different existing
 * account). A link attempt that just times out or never returns at all is treated as the user
 * cancelling, never as a silent trigger to pop open a second, unprompted browser tab.
 */
internal suspend fun awaitFacebookOAuthSession(): FacebookAuthOutcome {
    val linkResult = runOAuthRoundTrip { SupabaseClient.client.auth.linkIdentity(Facebook) }
    val session = when (linkResult) {
        is OAuthRoundTripResult.Session -> linkResult.session
        OAuthRoundTripResult.ExplicitFailure -> {
            val signInResult = runOAuthRoundTrip { SupabaseClient.client.auth.signInWith(Facebook) }
            (signInResult as? OAuthRoundTripResult.Session)?.session
        }
        null -> null
    }

    val result = session?.toFacebookAuthResult() ?: return FacebookAuthOutcome.CancelledOrFailed
    return FacebookAuthOutcome.SignedIn(result)
}

private suspend fun runOAuthRoundTrip(startFlow: suspend () -> Unit): OAuthRoundTripResult? {
    val deferred = CompletableDeferred<OAuthRoundTripResult?>()
    pendingOAuthResult = deferred
    deeplinkSeenForPendingAttempt = false

    try {
        startFlow()
    } catch (e: Exception) {
        pendingOAuthResult = null
        return OAuthRoundTripResult.ExplicitFailure
    }

    return try {
        withTimeoutOrNull(OAUTH_TIMEOUT_MS) { deferred.await() }
    } finally {
        pendingOAuthResult = null
    }
}

/**
 * Facebook's OAuth (unlike Google/Apple's native id-token SDKs used elsewhere in this app) hands
 * back profile data via Supabase's own `auth.identities`/`user_metadata`, not a client-side
 * credential object — provider key names vary (Facebook's Graph API isn't OIDC, so there's no
 * standard "sub" claim guaranteed), hence the fallback chain rather than one fixed key per field.
 */
private fun UserSession.toFacebookAuthResult(): AuthResult? {
    val identity = user?.identities?.firstOrNull { it.provider == "facebook" }
    val metadata: JsonObject? = identity?.identityData ?: user?.userMetadata
    val providerUserId = metadata?.get("sub")?.jsonPrimitive?.contentOrNull
        ?: metadata?.get("id")?.jsonPrimitive?.contentOrNull
        ?: identity?.id
        ?: user?.id
        ?: return null
    val email = user?.email ?: metadata?.get("email")?.jsonPrimitive?.contentOrNull
    val name = metadata?.get("name")?.jsonPrimitive?.contentOrNull
        ?: metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
    val avatarUrl = metadata?.get("picture")?.jsonPrimitive?.contentOrNull
        ?: metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
    return AuthResult(
        providerUserId = providerUserId,
        email = email ?: "",
        displayName = name ?: email ?: "Facebook",
        avatarUrl = avatarUrl
    )
}
