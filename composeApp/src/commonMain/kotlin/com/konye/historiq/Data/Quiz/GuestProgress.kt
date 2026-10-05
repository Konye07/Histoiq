package com.konye.historiq.Data.Quiz

import com.konye.historiq.Data.Remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException

private const val ROW_KEY = "user_id,game_key,date_key"

/**
 * A guest's results, kept across a sign-in that may land on a different account than the guest's own
 * anonymous one. Linking a new Google/Apple identity keeps the same auth.uid(), so nothing needs
 * moving; but when the identity already belongs to an existing account (a new phone, a reinstall, a
 * sign-out and back in) the sign-in switches to that account, and the guest's rows can no longer be
 * read — RLS is owner-only. [capture] therefore has to run while the guest's session is still the
 * current one, and [carryOverTo] once the account's session is.
 *
 * Best-effort: nothing here may fail the sign-in itself. It also runs inside AccountRepository's
 * session lock, so it talks to Postgrest with the uid in hand rather than through the stores, whose
 * ensureAnonymousSession() would wait on that same lock forever.
 */
internal class GuestProgress private constructor(
    private val guestUid: String,
    private val scores: List<ScoreHistoryRow>,
    private val progress: List<QuizProgressRow>
) {
    /**
     * Writes the captured rows under [accountId], whose session must be the current one. A row the
     * account already has for the same game and day wins — a guest never overwrites an existing
     * account's result — so the guest's rows only fill gaps: today's finished or half-played round
     * that the account has not touched, and past days for the streak and best score.
     */
    suspend fun carryOverTo(accountId: String) {
        if (accountId == guestUid) return
        try {
            val postgrest = SupabaseClient.client.postgrest
            if (scores.isNotEmpty()) {
                postgrest.from("score_history").upsert(scores.map { it.copy(userId = accountId) }) {
                    onConflict = ROW_KEY
                    ignoreDuplicates = true
                }
            }
            if (progress.isNotEmpty()) {
                postgrest.from("quiz_progress").upsert(progress.map { it.copy(userId = accountId) }) {
                    onConflict = ROW_KEY
                    ignoreDuplicates = true
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("Historiq: could not carry the guest's results over — ${e::class.simpleName}: ${e.message}")
        }
    }

    companion object {
        /**
         * Reads the current session's results: every score (a few bytes each, they feed the streak and
         * best score) but only today's round progress, the one day a round can still be resumed.
         * Null when there is no session or the read fails.
         */
        suspend fun capture(): GuestProgress? {
            val guestUid = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return null
            return try {
                val postgrest = SupabaseClient.client.postgrest
                GuestProgress(
                    guestUid,
                    scores = postgrest.from("score_history")
                        .select { filter { eq("user_id", guestUid) } }
                        .decodeList<ScoreHistoryRow>(),
                    progress = postgrest.from("quiz_progress")
                        .select { filter { eq("user_id", guestUid); eq("date_key", DailyClock.todayKey()) } }
                        .decodeList<QuizProgressRow>()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Historiq: could not read the guest's results — ${e::class.simpleName}: ${e.message}")
                null
            }
        }
    }
}
