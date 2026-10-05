package com.konye.historiq.Core.Auth

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.konye.historiq.Data.Remote.OAUTH_REDIRECT_HOST
import com.konye.historiq.Data.Remote.OAUTH_REDIRECT_SCHEME
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks

private const val TAG = "HistoriqAuth"
private const val RESUME_GRACE_PERIOD_MS = 700L

actual suspend fun signInWithFacebook(): FacebookAuthOutcome = awaitFacebookOAuthSession()

/** Called from [com.konye.historiq.Shared.MainActivity]'s onCreate/onNewIntent for every incoming
 * intent — completes whichever OAuth attempt (if any) is currently awaiting one. A no-op intent
 * (not a matching deep link) is a no-op call, see supabase-kt's own handleDeeplinks source.
 *
 * Checks for `error`/`error_code` query params itself (Supabase's standard OAuth-redirect error
 * convention, confirmed from supabase-kt's own UrlUtils.kt) *before* delegating to the library's
 * handleDeeplinks — that call only emits an auth event for them (its onError callback covers a
 * failed code exchange, not these), so this is the only way to distinguish an explicit failure
 * (e.g. `identity_already_exists`) from the user simply not completing the flow. */
fun handleFacebookOAuthDeeplink(intent: Intent) {
    val data = intent.data
    if (data?.scheme == OAUTH_REDIRECT_SCHEME && data.host == OAUTH_REDIRECT_HOST) {
        deeplinkSeenForPendingAttempt = true
        val errorCode = data.getQueryParameter("error_code") ?: data.getQueryParameter("error")
        if (errorCode != null) {
            Log.w(TAG, "Facebook sign-in: redirect carried an explicit error: $errorCode")
            pendingOAuthResult?.complete(OAuthRoundTripResult.ExplicitFailure)
            return
        }
    }
    SupabaseClient.client.handleDeeplinks(intent, onSessionSuccess = { session ->
        pendingOAuthResult?.complete(OAuthRoundTripResult.Session(session))
    })
}

/** Called from MainActivity's onResume — if the browser closed without ever producing a matching
 * deep link (the user backed out without completing sign-in), this resolves the pending attempt
 * as cancelled almost immediately instead of making them wait out the full timeout. Once a
 * matching deep link HAS been seen for this attempt, this backs off entirely — see
 * [deeplinkSeenForPendingAttempt]'s doc for why racing it against the async code exchange is wrong. */
fun notifyFacebookOAuthResumed() {
    val deferred = pendingOAuthResult ?: return
    if (deferred.isCompleted || deeplinkSeenForPendingAttempt) return
    Handler(Looper.getMainLooper()).postDelayed({
        if (!deferred.isCompleted && !deeplinkSeenForPendingAttempt) {
            Log.w(TAG, "Facebook sign-in: resumed with no deep link — treating as cancelled")
            deferred.complete(null)
        }
    }, RESUME_GRACE_PERIOD_MS)
}
