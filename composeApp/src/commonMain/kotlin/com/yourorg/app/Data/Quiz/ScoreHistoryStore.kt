package com.yourorg.app.Data.Quiz

import com.yourorg.app.Data.Account.AccountRepository
import com.yourorg.app.Data.Remote.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Every device's score history lives in the shared `score_history` table, keyed by a real
 * auth.uid() — a guest gets one too via [AccountRepository.ensureAnonymousSession], not just a
 * signed-in account — so a guest's single-day result still feeds the real daily distribution/rank
 * every player is compared against, and "once per game per day" is enforced server-side rather
 * than by a device-local flag that clearing app storage could reset. `score_history`'s foreign key
 * points at auth.users, not profiles, specifically so this works without a profile/display
 * identity. Only lifetime aggregates ([StatsStore]) are gated to real accounts.
 */
object ScoreHistoryStore {
    /** Idempotent per day — recording the same day's score again is a harmless overwrite with the same value. */
    suspend fun recordResult(quizKey: String, score: Double) {
        val userId = AccountRepository.ensureAnonymousSession()
        SupabaseClient.client.postgrest.from("score_history").upsert(
            ScoreHistoryRow(userId, quizKey, DailyClock.todayKey(), score)
        )
    }

    suspend fun resultFor(quizKey: String, dateKey: String): Double? {
        val userId = AccountRepository.ensureAnonymousSession()
        return SupabaseClient.client.postgrest.from("score_history")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("game_key", quizKey)
                    eq("date_key", dateKey)
                }
            }
            .decodeSingleOrNull<ScoreHistoryRow>()?.score
    }

    /** Any other signed-in user's score for a game/day — relies on score_history's friend-visible RLS policy. */
    suspend fun resultFor(otherUserId: String, quizKey: String, dateKey: String): Double? =
        SupabaseClient.client.postgrest.from("score_history")
            .select {
                filter {
                    eq("user_id", otherUserId)
                    eq("game_key", quizKey)
                    eq("date_key", dateKey)
                }
            }
            .decodeSingleOrNull<ScoreHistoryRow>()?.score
}

@Serializable
private data class ScoreHistoryRow(
    @SerialName("user_id") val userId: String,
    @SerialName("game_key") val gameKey: String,
    @SerialName("date_key") val dateKey: String,
    val score: Double
)
