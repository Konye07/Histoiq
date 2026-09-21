package com.konye.historiq.Data.Account

import com.konye.historiq.Core.Auth.AppleAuthOutcome
import com.konye.historiq.Core.Auth.GoogleAuthOutcome
import com.konye.historiq.Core.Auth.signInWithApple
import com.konye.historiq.Core.Auth.signInWithGoogle
import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Real accounts backed by Supabase: every device gets an anonymous auth.uid(), and real
 * "Continue with Google/Apple" (see [continueWithGoogle]/[continueWithApple]) attaches a real
 * OAuth identity to that one same session, preserving any guest progress already on it.
 * Guest mode never calls any of this; it stays fully local, as before.
 *
 * [currentAccount] is a synchronous read of the last-fetched profile so existing call sites
 * (Profile/Settings/Friends screens) don't all need to become suspend — it's populated by
 * [restoreSession] at app start and kept in sync by every mutating call here.
 */
object AccountRepository {
    private var cached: UserAccount? = null

    fun currentAccount(): UserAccount? = cached

    /** Call once at app start: resumes a previously-persisted anonymous session, if any. */
    suspend fun restoreSession(): UserAccount? {
        val auth = SupabaseClient.client.auth
        auth.awaitInitialization()
        val uid = auth.currentUserOrNull()?.id ?: return null
        return fetchProfile(uid)?.also { cached = it }
    }

    /**
     * Ensures *any* device — guest or signed-in — has a real anonymous Supabase session, so
     * day-scoped quiz data (score_history, quiz_progress) can be written under a real auth.uid()
     * even for a guest who never attaches a display identity. Safe to call repeatedly: a no-op
     * once a session (anonymous or real) already exists, and the same session is what
     * [continueWithGoogle]/[continueWithApple] later attach a profile to if the guest signs in.
     */
    suspend fun ensureAnonymousSession(): String {
        val auth = SupabaseClient.client.auth
        auth.awaitInitialization()
        if (auth.currentUserOrNull() == null) auth.signInAnonymously()
        return requireNotNull(auth.currentUserOrNull()?.id) { "Anonymous sign-in did not produce a session" }
    }

    /**
     * Real "Continue with Google" — see [com.konye.historiq.Core.Auth.signInWithGoogle]. Returns
     * null on cancellation/failure (caller shows the generic sign-in-failed message; never falls
     * back to a mock identity for a real attempt that didn't complete) — [GoogleAuthOutcome.
     * NotImplementedOnThisPlatform] is the one case that still does, same as before this existed.
     */
    suspend fun continueWithGoogle(): UserAccount? {
        val uid = ensureAnonymousSession()
        val existing = fetchProfile(uid)
        if (existing != null) {
            cached = existing
            runCatching { logDeviceEvent("sign_in") }
            return existing
        }

        val result = when (val outcome = signInWithGoogle()) {
            is GoogleAuthOutcome.SignedIn -> outcome.result
            GoogleAuthOutcome.NotImplementedOnThisPlatform -> mockSignIn(AuthProviderType.GOOGLE)
            GoogleAuthOutcome.CancelledOrFailed -> return null
        }

        // signInWithGoogle() can switch the active session out from under `uid`: if this Google
        // identity is already linked to a different existing account, linkIdentityWithIdToken
        // fails and its fallback signs in as THAT account instead. Inserting a profile under the
        // stale pre-sign-in uid then violates profiles' RLS (id must equal the CURRENT auth.uid())
        // and crashes with an uncaught PostgrestRestException — re-read the uid post-sign-in and
        // check for an existing profile again rather than assuming it's still the same session.
        val currentUid = requireNotNull(SupabaseClient.client.auth.currentUserOrNull()?.id) {
            "No session after Google sign-in"
        }
        val account = fetchProfile(currentUid) ?: insertProfileWithRetry(currentUid, AuthProviderType.GOOGLE, result)
        cached = account
        runCatching { logDeviceEvent("sign_in") }
        return account
    }

    /** Real "Continue with Apple" — mirrors [continueWithGoogle], see there for the stale-session-after-fallback caveat. */
    suspend fun continueWithApple(): UserAccount? {
        val uid = ensureAnonymousSession()
        val existing = fetchProfile(uid)
        if (existing != null) {
            cached = existing
            runCatching { logDeviceEvent("sign_in") }
            return existing
        }

        val result = when (val outcome = signInWithApple()) {
            is AppleAuthOutcome.SignedIn -> outcome.result
            AppleAuthOutcome.NotImplementedOnThisPlatform -> mockSignIn(AuthProviderType.APPLE)
            AppleAuthOutcome.CancelledOrFailed -> return null
        }

        val currentUid = requireNotNull(SupabaseClient.client.auth.currentUserOrNull()?.id) {
            "No session after Apple sign-in"
        }
        val account = fetchProfile(currentUid) ?: insertProfileWithRetry(currentUid, AuthProviderType.APPLE, result)
        cached = account
        runCatching { logDeviceEvent("sign_in") }
        return account
    }

    suspend fun updateDisplayName(newName: String): UserAccount? {
        val current = cached ?: return null
        SupabaseClient.client.postgrest.from("profiles")
            .update(DisplayNameUpdate(newName)) { filter { eq("id", current.id) } }
        val updated = current.copy(displayName = newName)
        cached = updated
        return updated
    }

    suspend fun signOut() {
        runCatching { SupabaseClient.client.auth.signOut() }
        cached = null
    }

    /**
     * Deletes every row this account owns explicitly, rather than trusting profiles' FK cascades
     * to game_stats/friendships to do it: each of these tables has RLS enabled, and a cascading
     * delete still has to satisfy the child table's own DELETE policy to actually remove rows, not
     * just the FK's ON DELETE CASCADE action. score_history/quiz_progress are FK'd to auth.users —
     * not profiles — so a guest can write them without ever having a profile row; they'd never be
     * touched by any profiles cascade regardless.
     */
    suspend fun deleteAccount() {
        val current = cached ?: return
        SupabaseClient.client.postgrest.from("score_history").delete { filter { eq("user_id", current.id) } }
        SupabaseClient.client.postgrest.from("quiz_progress").delete { filter { eq("user_id", current.id) } }
        SupabaseClient.client.postgrest.from("game_stats").delete { filter { eq("user_id", current.id) } }
        SupabaseClient.client.postgrest.from("friendships").delete {
            filter {
                or {
                    eq("requester_id", current.id)
                    eq("addressee_id", current.id)
                }
            }
        }
        SupabaseClient.client.postgrest.from("profiles").delete { filter { eq("id", current.id) } }
        runCatching { SupabaseClient.client.auth.signOut() }
        cached = null
    }

    private suspend fun fetchProfile(uid: String): UserAccount? =
        SupabaseClient.client.postgrest.from("profiles")
            .select(Columns.ALL) { filter { eq("id", uid) } }
            .decodeSingleOrNull<UserAccount>()

    private suspend fun insertProfileWithRetry(uid: String, provider: AuthProviderType, result: AuthResult): UserAccount {
        var lastError: Throwable? = null
        repeat(5) {
            val candidate = UserAccount(
                id = uid,
                provider = provider.id,
                providerUserId = result.providerUserId,
                email = result.email,
                displayName = result.displayName,
                avatarUrl = result.avatarUrl,
                friendCode = com.konye.historiq.Data.Social.randomFriendCode()
            )
            try {
                SupabaseClient.client.postgrest.from("profiles").insert(candidate)
                return candidate
            } catch (e: Throwable) {
                lastError = e
            }
        }
        throw lastError ?: IllegalStateException("Could not create profile")
    }

    private suspend fun logDeviceEvent(event: String) {
        SupabaseClient.client.postgrest.rpc("log_device_event", buildJsonObject { put("p_event", event) })
    }
}

@Serializable
private data class DisplayNameUpdate(@SerialName("display_name") val displayName: String)
