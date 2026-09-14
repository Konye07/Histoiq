package com.konye.historiq.Data.Account

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Real accounts backed by Supabase: every device gets an anonymous auth.uid() (no Google/Apple
 * OAuth credentials exist yet, per CLAUDE.md), and "Continue with Google/Apple" just attaches a
 * display identity (provider, mock email/name) to that one real session — see [continueWithProvider].
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
     * even for a guest who never attaches a display identity via [continueWithProvider]. Safe to
     * call repeatedly: a no-op once a session (anonymous or real) already exists, and the same
     * session is what [continueWithProvider] later attaches a profile to if the guest signs in.
     */
    suspend fun ensureAnonymousSession(): String {
        val auth = SupabaseClient.client.auth
        auth.awaitInitialization()
        if (auth.currentUserOrNull() == null) auth.signInAnonymously()
        return requireNotNull(auth.currentUserOrNull()?.id) { "Anonymous sign-in did not produce a session" }
    }

    /** Creates the profile on first sign-in with this provider identity, otherwise reuses the existing one. */
    suspend fun continueWithProvider(provider: AuthProviderType): UserAccount {
        val uid = ensureAnonymousSession()
        val existing = fetchProfile(uid)
        val account = existing ?: run {
            val result = mockSignIn(provider)
            insertProfileWithRetry(uid, provider, result)
        }
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
     * Deletes the profile (cascades to game_stats/friendships, which are still FK'd to it) plus
     * score_history/quiz_progress explicitly, since those two are now FK'd to auth.users — not
     * profiles — so a guest can write them without ever having a profile row.
     */
    suspend fun deleteAccount() {
        val current = cached ?: return
        SupabaseClient.client.postgrest.from("score_history").delete { filter { eq("user_id", current.id) } }
        SupabaseClient.client.postgrest.from("quiz_progress").delete { filter { eq("user_id", current.id) } }
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
